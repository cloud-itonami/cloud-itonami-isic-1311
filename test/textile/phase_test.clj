(ns textile.phase-test
  (:require [clojure.test :refer [deftest is]]
            [textile.phase :as phase]))

(deftest ^{:doc "New batch states initialize with intake phase."} batch-state-initialization
  (let [batch (phase/new-batch-state "batch-001" "mill-01" "cotton" 100.0)]
    (is (= "batch-001" (:batch-id batch)))
    (is (= "mill-01" (:plant-id batch)))
    (is (= "cotton" (:fiber-type batch)))
    (is (= 100.0 (:weight batch)))
    (is (= :intake (:phase batch)))
    (is (empty? (:events batch)))))

(deftest ^{:doc "Transition from intake to fiber-prep is valid."} valid-transition-intake-to-fiber-prep
  (is (phase/can-transition? :intake :fiber-prep)))

(deftest ^{:doc "Transition from fiber-prep to roving is valid."} valid-transition-fiber-prep-to-roving
  (is (phase/can-transition? :fiber-prep :roving)))

(deftest ^{:doc "Transition from spinning to winding is valid."} valid-transition-spinning-to-winding
  (is (phase/can-transition? :spinning :winding)))

(deftest ^{:doc "Invalid transitions are blocked."} invalid-transition-blocked
  (is (not (phase/can-transition? :intake :shipment)))
  (is (not (phase/can-transition? :shipment :intake))))

(deftest ^{:doc "Batch can transition through valid phase sequence."} phase-state-machine
  (let [batch (phase/new-batch-state "batch-002" "mill-02" "wool" 50.0)
        batch (phase/transition batch :fiber-prep)
        batch (phase/transition batch :roving)]
    (is (= :roving (:phase batch)))
    (is (= 2 (count (:events batch))))))

(deftest ^{:doc "Invalid transitions don't change state."} invalid-transition-preserved-state
  (let [batch (phase/new-batch-state "batch-003" "mill-03" "linen" 75.0)
        batch-before (phase/transition batch :fiber-prep)
        batch-after (phase/transition batch-before :shipment)]
    ;; Should still be in fiber-prep, not shipment
    (is (= :fiber-prep (:phase batch-after)))))

(deftest ^{:doc "Defects found in quality-check can escalate."} quality-check-can-lead-to-defect-escalation
  (is (phase/can-transition? :quality-check :defect-escalation)))

(deftest ^{:doc "Defects can lead to rework or rejection."} defect-escalation-can-lead-to-rework-or-reject
  (is (phase/can-transition? :defect-escalation :rework))
  (is (phase/can-transition? :defect-escalation :reject)))

(deftest ^{:doc "Reworked batches return to quality-check."} rework-returns-to-quality-check
  (is (phase/can-transition? :rework :quality-check)))

(deftest ^{:doc "Events can be logged in batch state."} log-event
  (let [batch (phase/new-batch-state "batch-004" "mill-04" "synthetic" 60.0)
        batch (phase/log-event batch :quality-test {:result :pass})]
    (is (= 1 (count (:events batch))))
    (is (= :quality-test (get-in batch [:events 0 :type])))))
