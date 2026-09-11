(ns textile.store
  "In-memory store for textile mill state: plants, batches, and verification records.")

;; ----------------------------- in-memory store factory -----------------------------

(defn mem-store
  "Create a new in-memory store for textile mill operations."
  []
  {:plants {}
   :batches {}
   :maintenance-records {}
   :quality-flags {}})

;; ----------------------------- plant registration & verification -----------------------------

(defn register-plant!
  "Register a plant in the store (side effect on mutable store map)."
  [st plant-id plant-name]
  (assoc-in st [:plants plant-id] {:id plant-id :name plant-name :verified false}))

(defn verify-plant!
  "Mark a plant as verified (side effect)."
  [st plant-id]
  (assoc-in st [:plants plant-id :verified] true))

(defn plant-registered?
  "Check if a plant is registered."
  [st plant-id]
  (contains? (:plants st) plant-id))

(defn plant-verified?
  "Check if a plant is registered AND verified."
  [st plant-id]
  (let [plant (get (:plants st) plant-id)]
    (and plant (:verified plant))))

;; ----------------------------- batch registration & verification -----------------------------

(defn register-batch!
  "Register a production batch in the store."
  [st batch-id plant-id fiber-type weight]
  (assoc-in st [:batches batch-id]
    {:id batch-id :plant-id plant-id :fiber-type fiber-type :weight weight :verified false}))

(defn verify-batch!
  "Mark a batch as verified (side effect)."
  [st batch-id]
  (assoc-in st [:batches batch-id :verified] true))

(defn batch-registered?
  "Check if a batch is registered."
  [st batch-id]
  (contains? (:batches st) batch-id))

(defn batch-verified?
  "Check if a batch is registered AND verified."
  [st batch-id]
  (let [batch (get (:batches st) batch-id)]
    (and batch (:verified batch))))

(defn get-batch
  "Retrieve a batch record by ID."
  [st batch-id]
  (get (:batches st) batch-id))

;; ----------------------------- maintenance records -----------------------------

(defn log-maintenance!
  "Log a maintenance record in the store."
  [st plant-id maintenance-type maintenance-date]
  (let [rec-id (str plant-id "-maint-" (count (:maintenance-records st)))]
    (assoc-in st [:maintenance-records rec-id]
      {:id rec-id :plant-id plant-id :type maintenance-type :date maintenance-date})))

;; ----------------------------- quality flags (escalations) -----------------------------

(defn flag-quality-issue!
  "Log a quality issue flag (escalation) in the store."
  [st batch-id defect-type severity]
  (let [flag-id (str batch-id "-quality-" (count (:quality-flags st)))]
    (assoc-in st [:quality-flags flag-id]
      {:id flag-id :batch-id batch-id :defect defect-type :severity severity :escalated true})))

(defn has-quality-flags?
  "Check if a batch has any quality flags."
  [st batch-id]
  (some #(= batch-id (:batch-id %)) (vals (:quality-flags st))))
