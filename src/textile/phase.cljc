(ns textile.phase
  "State machine phases for textile production operations (ISIC 1311).
  Models the lifecycle of a batch from raw material intake through shipment.")

;; ----------------------------- batch lifecycle phases -----------------------------

(def phases
  {:intake {:name "Material Intake"
            :description "Raw fiber received, documented, verified"
            :transitions #{:fiber-prep}}

   :fiber-prep {:name "Fiber Preparation"
                :description "Fiber opened, cleaned, blended"
                :transitions #{:roving}}

   :roving {:name "Roving Production"
            :description "Fibers drafted and twisted into roving"
            :transitions #{:spinning :quality-check}}

   :spinning {:name "Spinning"
              :description "Roving spun into yarn on spinning frames"
              :transitions #{:quality-check :winding}}

   :winding {:name "Winding"
             :description "Yarn wound onto bobbins or cones"
             :transitions #{:quality-check :finish}}

   :quality-check {:name "Quality Control"
                   :description "Yarn tested for strength, evenness, contamination"
                   :transitions #{:finish :defect-escalation}}

   :defect-escalation {:name "Defect Handling"
                       :description "Quality issues escalated, root-cause analysis"
                       :transitions #{:rework :reject}}

   :rework {:name "Rework"
            :description "Fiber or yarn reworked to meet quality standards"
            :transitions #{:quality-check}}

   :reject {:name "Rejection"
            :description "Batch rejected for non-compliance"
            :transitions #{}}

   :finish {:name "Packaging & Finish"
            :description "Yarn packaged for shipment"
            :transitions #{:shipment}}

   :shipment {:name "Shipment"
              :description "Yarn shipped to customer"
              :transitions #{}}})

;; ----------------------------- state initialization & transitions -----------------------------

(defn new-batch-state
  "Create a new batch production state."
  [batch-id plant-id fiber-type weight]
  {:batch-id batch-id
   :plant-id plant-id
   :fiber-type fiber-type
   :weight weight
   :phase :intake
   :timestamp #?(:clj (System/currentTimeMillis) :cljs (js/Date.))
   :events []
   :quality-flags []})

(defn can-transition?
  "Check if a transition from current-phase to next-phase is valid."
  [current-phase next-phase]
  (let [valid-transitions (get-in phases [current-phase :transitions])]
    (contains? valid-transitions next-phase)))

(defn transition
  "Transition a batch to a new phase, if valid."
  [batch-state next-phase]
  (let [current-phase (:phase batch-state)]
    (if (can-transition? current-phase next-phase)
      (-> batch-state
          (assoc :phase next-phase)
          (update :events conj {:from current-phase :to next-phase :timestamp #?(:clj (System/currentTimeMillis) :cljs (js/Date.))}))
      batch-state)))

(defn log-event
  "Log an event in batch state."
  [batch-state event-type event-data]
  (update batch-state :events conj
    {:type event-type :data event-data :timestamp #?(:clj (System/currentTimeMillis) :cljs (js/Date.))}))
