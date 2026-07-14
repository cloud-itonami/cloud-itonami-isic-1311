(ns textile.registry
  "Registry of proposal drafts for testing and verification.")

;; ----------------------------- proposal templates for testing -----------------------------

(defn batch-logging-draft
  "Create a batch logging proposal draft."
  [batch-id cites evidence confidence detail]
  {:op :actuation/log-production-batch
   :subject batch-id
   :effect :propose
   :cites cites
   :value {:evidence evidence :confidence confidence :detail detail}})

(defn maintenance-draft
  "Create a maintenance scheduling proposal draft."
  [plant-id cites evidence confidence detail]
  {:op :actuation/schedule-maintenance
   :subject plant-id
   :effect :propose
   :cites cites
   :value {:evidence evidence :confidence confidence :detail detail}})

(defn quality-defect-draft
  "Create a quality defect flag proposal draft."
  [batch-id cites evidence confidence defect-type severity detail]
  {:op :actuation/flag-quality-defect
   :subject batch-id
   :effect :propose
   :cites cites
   :value {:evidence evidence :confidence confidence :defect-type defect-type
           :severity severity :detail detail}})

(defn shipment-draft
  "Create a shipment coordination proposal draft."
  [batch-id cites evidence confidence destination quantity detail]
  {:op :actuation/coordinate-shipment
   :subject batch-id
   :effect :propose
   :cites cites
   :value {:evidence evidence :confidence confidence :destination destination
           :quantity quantity :detail detail}})
