(ns textile.governor
  "Textile Mill Operations Governor -- the independent compliance layer that earns
  the Textile Operations Advisor the right to propose and log actions.
  The LLM has no notion of textile mill safety standards, labor regulations,
  or when production-batch logging or maintenance scheduling is a real-world actuation,
  so this MUST be a separate system able to *reject* a proposal and fall back
  to HOLD.

  HARD violations (a human approver CANNOT override):
    1. Spec-basis       -- no official jurisdiction citation
    2. Plant verification -- plant/batch record must be verified/registered
    3. Quality escalation -- quality defects ALWAYS escalate (never silent log)
    4. Process-control operations -- NO direct spinning/carding-line control
                                     (those remain mill engineer exclusive authority)

  SOFT violation (can be approved by human):
    5. Confidence floor / actuation gate -- low confidence OR real actuation

  CRITICAL SCOPE BOUNDARY:
  This actor coordinates LOGISTICS and COMPLIANCE PAPERWORK around textile
  production. It does NOT:
    - Control spinning frame/carding machine operation
    - Control fibre tension, speed, or other process parameters
    - Operate fibre-handling equipment directly
    - Make process-engineering decisions about fibre quality/composition blending

  Those remain the exclusive authority of licensed mill engineers."
  (:require [textile.store :as store]))

(def confidence-floor 0.6)

(def high-stakes
  "Operations that require human sign-off for real-world actuation:
  Production-batch logging and quality defect flags with escalation."
  #{:actuation/log-production-batch :actuation/flag-quality-defect})

(def process-control-keywords
  "Words that indicate process-engineering authority (FORBIDDEN for this actor).
  If a proposal mentions any of these, it's a hard block."
  #{"spinning-control" "carding-control" "tension" "speed" "rpm"
    "fibre-feed" "twist-rate" "frame-control" "drawing-control"
    "roving-control" "process-parameters" "machine-parameters"})

;; ----------------------------- checks -----------------------------

(defn- spec-basis-violations
  "A proposal with no spec-basis citation is a HARD violation --
  never invent a jurisdiction's requirements."
  [proposal _st]
  (let [op (:op proposal)]
    (when (contains? #{:actuation/log-production-batch
                       :actuation/flag-quality-defect
                       :actuation/schedule-maintenance} op)
      (when (or (empty? (:cites proposal))
                (and (contains? (:value proposal) :spec-basis)
                     (nil? (:spec-basis (:value proposal)))))
        [{:rule :no-spec-basis
          :detail "公式な仕様基準の引用が無い提案は処理できない"}]))))

(defn- plant-verification-violations
  "For batch operations, the owning plant must be verified.
  For plant operations (maintenance), the plant itself must be verified."
  [proposal st]
  (let [op (:op proposal)
        subject (:subject proposal)]
    (when (= op :actuation/schedule-maintenance)
      ;; Maintenance on a plant - check if plant is verified
      (when-not (store/plant-verified? st subject)
        [{:rule :plant-not-verified
          :detail "工場のライセンス / 登録が未確認"}]))))

(defn- batch-verification-violations
  "Batch record must be verified/registered before operations,
  and the owning plant must also be verified."
  [{:keys [op subject]} st]
  (when (contains? #{:actuation/log-production-batch
                     :actuation/flag-quality-defect
                     :actuation/coordinate-shipment} op)
    (let [batch (store/get-batch st subject)
          plant-id (:plant-id batch)]
      (cond
        (not (store/batch-verified? st subject))
        [{:rule :batch-not-verified
          :detail "生産ロットが未検証 / 未登録"}]

        (and plant-id (not (store/plant-verified? st plant-id)))
        [{:rule :plant-not-verified
          :detail "生産ロットの所有工場が未検証"}]

        :else nil))))

(defn- process-control-block-violations
  "HARD BLOCK: This actor does NOT make process-engineering decisions.
  If a proposal mentions spinning/carding control, machine parameters, or other
  process controls, reject it immediately.
  Those decisions remain the exclusive authority of licensed mill engineers."
  [proposal _st]
  (let [detail (str (:detail (:value proposal)) " " (:op proposal))
        words (re-seq #"\w+" (.toLowerCase detail))
        forbidden (some #(contains? process-control-keywords %) words)]
    (when forbidden
      [{:rule :process-control-forbidden
        :detail (str "紡績機制御は認可エンジニアの排他的権限です。"
                    "この提案には禁止キーワード '" forbidden "' が含まれています。")}])))

(defn- quality-defect-escalation-violations
  "If quality defect is flagged, this MUST escalate to human.
  Never silently log a quality issue."
  [{:keys [op]} _st]
  (when (= op :actuation/flag-quality-defect)
    [{:rule :quality-defect-escalation
      :detail "品質不良フラグは必ず人間にエスカレートされる"}]))

(defn- confidence-gate-violations
  "Low confidence or high-stakes actuation -> escalate to human."
  [{:keys [op]} {:keys [confidence]}]
  (let [confidence (or confidence 0.5)]
    (when (or (< confidence confidence-floor)
              (contains? high-stakes op))
      [{:rule :escalate
        :detail (if (< confidence confidence-floor)
                  (str "信頼度が低い (confidence=" confidence ")")
                  "実際の操作には人間の承認が必要")}])))

;; ----------------------------- governor evaluation -----------------------------

(defn evaluate
  "Evaluate a proposal against all hard and soft gates.
  Returns a map:
    {:holds? boolean
     :hard-violations [...]
     :soft-violations [...]
     :clean? boolean}"
  [proposal st]
  (let [hard-checks-store [spec-basis-violations
                           plant-verification-violations
                           batch-verification-violations
                           process-control-block-violations]
        hard-checks-value [quality-defect-escalation-violations]
        soft-checks [confidence-gate-violations]
        hard-violations-store (mapcat #(% proposal st) hard-checks-store)
        hard-violations-value (mapcat #(% proposal (:value proposal)) hard-checks-value)
        hard-violations (concat hard-violations-store hard-violations-value)
        soft-violations (mapcat #(% proposal (:value proposal)) soft-checks)]
    {:holds? (seq hard-violations)
     :hard-violations (vec hard-violations)
     :soft-violations (vec soft-violations)
     :clean? (and (empty? hard-violations) (empty? soft-violations))}))
