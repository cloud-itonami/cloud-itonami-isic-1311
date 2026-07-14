(ns textile.advisor
  "Textile Operations Advisor -- the LLM-driven suggestion layer.
  Proposes operations to the Governor for approval.")

;; ----------------------------- mock advisor for testing -----------------------------

(defn mock-advisor
  "Create a mock advisor for testing. Real implementation would call an LLM."
  []
  {:type :mock :model "mock-v1"})

(defn batch-logging-proposal
  "Propose a production batch logging operation."
  [_advisor batch-id]
  {:op :actuation/log-production-batch
   :subject batch-id
   :effect :propose
   :cites ["Labor Standards Act (労働基準法) §35" "Textile Safety Regulations (繊維安全規則) §12"]
   :value {:evidence {:batch-registered true :fiber-quality-report true :output-weight true}
           :confidence 0.87
           :detail "Production batch completed and verified, ready for logging"}})

(defn maintenance-proposal
  "Propose a maintenance scheduling operation."
  [_advisor plant-id maintenance-type]
  {:op :actuation/schedule-maintenance
   :subject plant-id
   :effect :propose
   :cites ["Industrial Safety and Health Act (労働安全衛生法) §20"]
   :value {:evidence {:inspection-report true :maintenance-log true :parts-available true}
           :confidence 0.85
           :detail (str "Scheduled " maintenance-type " maintenance for equipment safety compliance")}})

(defn quality-defect-proposal
  "Propose a quality defect flag (always escalates)."
  [_advisor batch-id defect-type severity]
  {:op :actuation/flag-quality-defect
   :subject batch-id
   :effect :propose
   :cites ["Quality Management Standards (品質管理基準) §8"]
   :value {:evidence {:defect-observation true :sample-retained true :root-cause-analysis false}
           :confidence 0.82
           :defect-type defect-type
           :severity severity
           :detail (str "Quality defect detected: " defect-type " - " severity " impact - immediate escalation required")}})

(defn shipment-coordination-proposal
  "Propose a shipment coordination operation."
  [_advisor batch-id destination quantity]
  {:op :actuation/coordinate-shipment
   :subject batch-id
   :effect :propose
   :cites ["International Trade Regulations (国際貿易規則) §15"]
   :value {:evidence {:batch-quality-verified true :packaging-certified true :transport-arranged true}
           :confidence 0.88
           :destination destination
           :quantity quantity
           :detail "Production batch ready for coordinated shipment"}})
