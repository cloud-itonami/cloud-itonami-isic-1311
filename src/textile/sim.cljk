(ns textile.sim
  "Simulation of textile production operations actor -- demonstrates Governor,
  Advisor, and Store working together."
  (:require [textile.advisor :as advisor]
           [textile.governor :as governor]
           [textile.store :as store]
           [textile.phase :as phase]))

;; ----------------------------- demo functions for REPL / testing -----------------------------

(defn demo-successful-batch-logging
  "Demonstrate a successful batch logging workflow."
  []
  (let [st (store/mem-store)
        adv (advisor/mock-advisor)
        ;; Register and verify plant
        st (store/register-plant! st "mill-01" "Heritage Textile Mill")
        st (store/verify-plant! st "mill-01")
        ;; Register and verify batch
        st (store/register-batch! st "batch-001" "mill-01" "cotton" 100.0)
        st (store/verify-batch! st "batch-001")
        ;; Generate batch logging proposal
        proposal (advisor/batch-logging-proposal adv "batch-001")
        ;; Evaluate through Governor
        eval-result (governor/evaluate proposal st)]
    {:proposal proposal
     :evaluation eval-result
     :store st}))

(defn demo-process-control-block
  "Demonstrate that process-control proposals are blocked."
  []
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-02" "Industrial Spinning Mill")
        st (store/verify-plant! st "mill-02")
        proposal {:op :actuation/log-production-batch
                  :subject "batch-002"
                  :effect :propose
                  :cites ["Textile Safety Regulations §12"]
                  :value {:evidence {:batch-registered true}
                          :confidence 0.9
                          :detail "Please increase spindle speed to 4200 RPM for faster output"}}
        eval-result (governor/evaluate proposal st)]
    {:proposal proposal
     :evaluation eval-result
     :store st}))

(defn demo-quality-defect-escalation
  "Demonstrate that quality defects always escalate."
  []
  (let [st (store/mem-store)
        adv (advisor/mock-advisor)
        st (store/register-plant! st "mill-03" "Quality-Conscious Mill")
        st (store/verify-plant! st "mill-03")
        st (store/register-batch! st "batch-003" "mill-03" "wool" 50.0)
        st (store/verify-batch! st "batch-003")
        ;; Generate quality defect proposal
        proposal (advisor/quality-defect-proposal adv "batch-003" "contamination" "high")
        ;; Evaluate through Governor
        eval-result (governor/evaluate proposal st)]
    {:proposal proposal
     :evaluation eval-result
     :escalation? (seq (:hard-violations eval-result))}))

(defn demo-batch-not-verified
  "Demonstrate that operations on unverified batches are blocked."
  []
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-04" "Unvetted Mill")
        ;; Do NOT verify the plant
        st (store/register-batch! st "batch-004" "mill-04" "synthetic" 75.0)
        ;; Do NOT verify the batch
        proposal {:op :actuation/log-production-batch
                  :subject "batch-004"
                  :effect :propose
                  :cites ["Quality Management Standards §8"]
                  :value {:evidence {:batch-registered true}
                          :confidence 0.8
                          :detail "Batch logging"}}
        eval-result (governor/evaluate proposal st)]
    {:proposal proposal
     :evaluation eval-result
     :blocked-by? (seq (:hard-violations eval-result))}))

(defn demo-phase-transitions
  "Demonstrate batch state machine phase transitions."
  []
  (let [batch-state (phase/new-batch-state "batch-005" "mill-05" "flax" 200.0)]
    {:initial-state batch-state
     :after-fiber-prep (phase/transition batch-state :fiber-prep)
     :after-roving (phase/transition (phase/transition batch-state :fiber-prep) :roving)
     :after-quality-check (phase/transition
                            (phase/transition
                             (phase/transition batch-state :fiber-prep) :roving) :quality-check)}))
