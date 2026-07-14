(ns textile.phase-test
  (:require [clojure.test :refer [deftest is]]
            [textile.phase :as phase]))

(deftest batch-state-initialization
  "New batch states initialize with intake phase."
  (let [batch (phase/new-batch-state "batch-001" "mill-01" "cotton" 100.0)]
    (is (= "batch-001" (:batch-id batch)))
    (is (= "mill-01" (:plant-id batch)))
    (is (= "cotton" (:fiber-type batch)))
    (is (= 100.0 (:weight batch)))
    (is (= :intake (:phase batch)))
    (is (empty? (:events batch)))))

(deftest valid-transition-intake-to-fiber-prep
  "Transition from intake to fiber-prep is valid."
  (is (phase/can-transition? :intake :fiber-prep)))

(deftest valid-transition-fiber-prep-to-roving
  "Transition from fiber-prep to roving is valid."
  (is (phase/can-transition? :fiber-prep :roving)))

(deftest valid-transition-spinning-to-winding
  "Transition from spinning to winding is valid."
  (is (phase/can-transition? :spinning :winding)))

(deftest invalid-transition-blocked
  "Invalid transitions are blocked."
  (is (not (phase/can-transition? :intake :shipment)))
  (is (not (phase/can-transition? :shipment :intake))))

(deftest phase-state-machine
  "Batch can transition through valid phase sequence."
  (let [batch (phase/new-batch-state "batch-002" "mill-02" "wool" 50.0)
        batch (phase/transition batch :fiber-prep)
        batch (phase/transition batch :roving)]
    (is (= :roving (:phase batch)))
    (is (= 2 (count (:events batch))))))

(deftest invalid-transition-preserved-state
  "Invalid transitions don't change state."
  (let [batch (phase/new-batch-state "batch-003" "mill-03" "linen" 75.0)
        batch-before (phase/transition batch :fiber-prep)
        batch-after (phase/transition batch-before :shipment)]
    ;; Should still be in fiber-prep, not shipment
    (is (= :fiber-prep (:phase batch-after)))))

(deftest quality-check-can-lead-to-defect-escalation
  "Defects found in quality-check can escalate."
  (is (phase/can-transition? :quality-check :defect-escalation)))

(deftest defect-escalation-can-lead-to-rework-or-reject
  "Defects can lead to rework or rejection."
  (is (phase/can-transition? :defect-escalation :rework))
  (is (phase/can-transition? :defect-escalation :reject)))

(deftest rework-returns-to-quality-check
  "Reworked batches return to quality-check."
  (is (phase/can-transition? :rework :quality-check)))

(deftest log-event
  "Events can be logged in batch state."
  (let [batch (phase/new-batch-state "batch-004" "mill-04" "synthetic" 60.0)
        batch (phase/log-event batch :quality-test {:result :pass})]
    (is (= 1 (count (:events batch))))
    (is (= :quality-test (get-in batch [:events 0 :type])))))
