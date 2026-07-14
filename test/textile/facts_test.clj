(ns textile.facts-test
  (:require [clojure.test :refer [deftest is]]
            [textile.facts :as facts]))

(deftest regulations-defined
  "Regulatory references are defined for textile industry."
  (is (contains? facts/regulations :labor-standards-act))
  (is (contains? facts/regulations :textile-safety-regulations))
  (is (contains? facts/regulations :industrial-safety-act))
  (is (contains? facts/regulations :quality-management-standards))
  (is (contains? facts/regulations :environmental-protection))
  (is (contains? facts/regulations :international-trade-regulations)))

(deftest process-phases-defined
  "ISIC 1311 process phases are documented."
  (is (contains? facts/process-phases :fiber-preparation))
  (is (contains? facts/process-phases :roving-production))
  (is (contains? facts/process-phases :spinning))
  (is (contains? facts/process-phases :quality-control)))

(deftest equipment-categories-defined
  "Equipment categories for textile mills are documented."
  (is (contains? facts/equipment-categories :fiber-prep))
  (is (contains? facts/equipment-categories :roving-prod))
  (is (contains? facts/equipment-categories :spinning))
  (is (contains? facts/equipment-categories :finishing)))

(deftest safety-thresholds-defined
  "Safety thresholds for textile operations are set."
  (is (contains? facts/safety-thresholds :max-fiber-contamination-ppm))
  (is (contains? facts/safety-thresholds :max-noise-level-db))
  (is (contains? facts/safety-thresholds :required-maintenance-interval-days))
  (is (contains? facts/safety-thresholds :max-spindle-speed-rpm)))

(deftest social-impact-factors-defined
  "Social impact factors for ISIC 1311 are documented."
  (is (contains? facts/social-impact :fair-labor))
  (is (contains? facts/social-impact :local-industry))
  (is (contains? facts/social-impact :circular-materials)))
