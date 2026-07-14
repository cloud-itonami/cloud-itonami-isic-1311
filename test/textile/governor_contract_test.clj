(ns textile.governor-contract-test
  (:require [clojure.test :refer [deftest is]]
            [textile.store :as store]
            [textile.advisor :as advisor]
            [textile.governor :as governor]
            [textile.registry :as registry]))

(deftest spec-basis-hard-gate
  "Spec-basis is a HARD gate: never allow proposals without official citations."
  (let [st (store/mem-store)
        proposal {:op :actuation/log-production-batch
                  :subject "batch-001"
                  :effect :propose
                  :value {:evidence {:batch-registered true}
                          :confidence 0.9}
                  :cites []}]
    (let [eval (governor/evaluate proposal st)]
      (is (:holds? eval) "Proposal with empty cites should hold")
      (is (seq (:hard-violations eval)) "Should have hard violations")
      (is (some #(= (:rule %) :no-spec-basis) (:hard-violations eval))))))

(deftest plant-not-verified-blocks
  "Operations with unverified owning plants are blocked."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-01" "Test Mill")
        ;; Do NOT verify the plant
        st (store/register-batch! st "batch-001" "mill-01" "cotton" 100.0)
        st (store/verify-batch! st "batch-001")  ;; Verify batch but not plant
        proposal (registry/batch-logging-draft "batch-001"
                   ["Textile Safety Regulations §12"]
                   {:batch-registered true}
                   0.9
                   "Batch logging")]
    (let [eval (governor/evaluate proposal st)]
      (is (seq (:hard-violations eval)) "Should have hard violations")
      (is (some #(= (:rule %) :plant-not-verified) (:hard-violations eval))))))

(deftest batch-not-verified-blocks
  "Operations on unverified batches are blocked."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-02" "Test Mill")
        st (store/verify-plant! st "mill-02")
        st (store/register-batch! st "batch-002" "mill-02" "cotton" 100.0)
        ;; Do NOT verify the batch
        proposal (registry/batch-logging-draft "batch-002"
                   ["Textile Safety Regulations §12"]
                   {:batch-registered true}
                   0.9
                   "Batch logging")]
    (let [eval (governor/evaluate proposal st)]
      (is (seq (:hard-violations eval)) "Should have hard violations")
      (is (some #(= (:rule %) :batch-not-verified) (:hard-violations eval))))))

(deftest process-control-block
  "HARD BLOCK: Proposals mentioning spinning control, frame control, or process
  parameters are immediately rejected. Those remain engineer exclusive authority."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-03" "Test Mill")
        st (store/verify-plant! st "mill-03")
        st (store/register-batch! st "batch-003" "mill-03" "wool" 50.0)
        st (store/verify-batch! st "batch-003")
        proposal {:op :actuation/log-production-batch
                  :subject "batch-003"
                  :effect :propose
                  :cites ["some-spec"]
                  :value {:evidence {:batch-registered true}
                          :confidence 0.9
                          :detail "Please increase spindle speed to 4200 RPM and adjust frame tension"}}]
    (let [eval (governor/evaluate proposal st)]
      (is (:holds? eval) "Process-control proposal should hold")
      (is (some #(= (:rule %) :process-control-forbidden) (:hard-violations eval))
        "Should have process-control-forbidden violation"))))

(deftest quality-defect-escalation
  "Quality defect flags ALWAYS escalate to human.
  Never silently log a quality issue."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-04" "Test Mill")
        st (store/verify-plant! st "mill-04")
        st (store/register-batch! st "batch-004" "mill-04" "synthetic" 75.0)
        st (store/verify-batch! st "batch-004")
        proposal {:op :actuation/flag-quality-defect
                  :subject "batch-004"
                  :effect :propose
                  :cites ["Quality Management Standards §8"]
                  :value {:evidence {:defect-observation true}
                          :confidence 0.95
                          :defect-type "contamination"
                          :severity "high"
                          :detail "Foreign fiber contamination detected"}}]
    (let [eval (governor/evaluate proposal st)]
      (is (:holds? eval) "Quality defect should hold")
      (is (some #(= (:rule %) :quality-defect-escalation) (:hard-violations eval))
        "Should have quality-defect-escalation violation"))))

(deftest actuation-requires-escalation
  "Both batch logging and quality defect flagging require human sign-off,
  even when all other checks are clean."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-05" "Test Mill")
        st (store/verify-plant! st "mill-05")
        st (store/register-batch! st "batch-005" "mill-05" "cotton" 100.0)
        st (store/verify-batch! st "batch-005")
        adv (advisor/mock-advisor)
        batch-proposal (advisor/batch-logging-proposal adv "batch-005")]
    (let [eval (governor/evaluate batch-proposal st)]
      (is (seq (:soft-violations eval)) "Should have soft violations for actuation")
      (is (some #(= (:rule %) :escalate) (:soft-violations eval))
        "Should escalate high-stakes actuation"))))

(deftest maintenance-scheduling-allowed
  "Maintenance scheduling is allowed for verified plants."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-06" "Test Mill")
        st (store/verify-plant! st "mill-06")
        proposal (registry/maintenance-draft "mill-06"
                   ["Industrial Safety and Health Act §20"]
                   {:inspection-report true}
                   0.85
                   "Scheduled preventive maintenance")]
    (let [eval (governor/evaluate proposal st)]
      ;; No hard violations for verified plant
      (is (empty? (:hard-violations eval)) "Should have no hard violations for verified plant"))))

(deftest shipment-coordination-requires-batch-verification
  "Shipment coordination requires both plant and batch to be verified."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-07" "Test Mill")
        st (store/verify-plant! st "mill-07")
        st (store/register-batch! st "batch-007" "mill-07" "linen" 60.0)
        ;; Do NOT verify batch
        proposal (registry/shipment-draft "batch-007"
                   ["International Trade Regulations §15"]
                   {:batch-quality-verified true}
                   0.88
                   "Export shipment"
                   "USA"
                   "100 kg")]
    (let [eval (governor/evaluate proposal st)]
      (is (some #(= (:rule %) :batch-not-verified) (:hard-violations eval))
        "Should block shipment with unverified batch"))))

(deftest confidence-floor-triggers-soft-gate
  "Low confidence (<0.6) triggers soft-gate escalation."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-08" "Test Mill")
        st (store/verify-plant! st "mill-08")
        st (store/register-batch! st "batch-008" "mill-08" "cotton" 100.0)
        st (store/verify-batch! st "batch-008")
        proposal (registry/batch-logging-draft "batch-008"
                   ["Textile Safety Regulations §12"]
                   {:batch-registered true}
                   0.5  ;; Below confidence-floor (0.6)
                   "Batch logging with low confidence")]
    (let [eval (governor/evaluate proposal st)]
      (is (seq (:soft-violations eval)) "Should have soft violations")
      (is (some #(= (:rule %) :escalate) (:soft-violations eval))
        "Should escalate on low confidence"))))

(deftest clean-proposal-passes-all-gates
  "A complete, well-formed high-stakes proposal with all verifications has soft escalation gate."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-09" "Test Mill")
        st (store/verify-plant! st "mill-09")
        st (store/register-batch! st "batch-009" "mill-09" "wool" 50.0)
        st (store/verify-batch! st "batch-009")
        proposal (registry/batch-logging-draft "batch-009"
                   ["Textile Safety Regulations §12"]
                   {:batch-registered true}
                   0.85
                   "Batch logging")]
    (let [eval (governor/evaluate proposal st)]
      ;; No hard violations for verified plant and batch
      (is (empty? (:hard-violations eval)) "Should have no hard violations")
      ;; High-stakes actuation (batch logging) always escalates
      (is (seq (:soft-violations eval)) "High-stakes actuation has soft gate")
      (is (some #(= (:rule %) :escalate) (:soft-violations eval))))))
