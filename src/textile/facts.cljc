(ns textile.facts
  "Regulatory and domain-specific facts about textile industry and ISIC 1311 (fiber preparation & spinning).")

;; ----------------------------- regulatory references (Japan) -----------------------------

(def regulations
  {;; Labor/Safety
   :labor-standards-act "Labor Standards Act (労働基準法) - Working hours, safety, rest periods"
   :textile-safety-regulations "Textile Safety Regulations (繊維安全規則) - Equipment safety, guarding"
   :industrial-safety-act "Industrial Safety and Health Act (労働安全衛生法) - Occupational health & safety"

   ;; Quality/Environment
   :quality-management-standards "Quality Management Standards (品質管理基準) - Fiber quality, testing"
   :environmental-protection "Environmental Protection Act (環境保全法) - Water/air quality in textile operations"
   :chemical-safety "Chemical Safety Regulations (化学物質安全規則) - Sizing chemicals, dyes"

   ;; Trade/International
   :international-trade-regulations "International Trade Regulations (国際貿易規則) - Export/import compliance"
   :fair-trade-certification "Fair Trade Certification Standards - Labor equity, supply chain"})

;; ----------------------------- process phases (ISIC 1311) -----------------------------

(def process-phases
  {:fiber-preparation {:name "Fiber Preparation"
                       :steps ["Opening" "Cleaning" "Carding" "Combing"]}
   :roving-production {:name "Roving Production"
                       :steps ["Drawing" "Twisting" "Winding"]}
   :spinning {:name "Spinning"
              :steps ["Ring spinning" "Frame spinning" "Twist insertion" "Winding"]}
   :quality-control {:name "Quality Control"
                     :steps ["Tensile testing" "Linear density testing" "Evenness testing" "Contamination checking"]}})

;; ----------------------------- equipment categories (textile mills) -----------------------------

(def equipment-categories
  {:fiber-prep ["Card machines" "Combs" "Openers" "Blenders"]
   :roving-prod ["Drafting frames" "Twisting units" "Winding equipment"]
   :spinning ["Ring frames" "Spindles" "Delivery systems" "Bobbins"]
   :finishing ["Winders" "Cone winders" "Packaging equipment"]})

;; ----------------------------- safety thresholds (ISIC 1311) -----------------------------

(def safety-thresholds
  {:max-fiber-contamination-ppm 50      ; Parts per million
   :max-noise-level-db 85               ; Decibels
   :required-maintenance-interval-days 30
   :max-spindle-speed-rpm 4000})

;; ----------------------------- social impact factors for ISIC 1311 -----------------------------

(def social-impact
  {:fair-labor "Fair labor practices - wages, working hours"
   :local-industry "Support for local textile industry and artisan traditions"
   :circular-materials "Circular economy - waste reduction, recycled fiber usage"
   :community-development "Community skill development and employment"
   :environmental-stewardship "Water usage reduction, chemical management"})
