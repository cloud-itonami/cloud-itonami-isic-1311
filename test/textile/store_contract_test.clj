(ns textile.store-contract-test
  (:require [clojure.test :refer [deftest is]]
            [textile.store :as store]))

(deftest mem-store-initialization
  "In-memory store initializes with empty collections."
  (let [st (store/mem-store)]
    (is (= {} (:plants st)))
    (is (= {} (:batches st)))
    (is (= {} (:maintenance-records st)))
    (is (= {} (:quality-flags st)))))

(deftest plant-registration
  "Plants can be registered in the store."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-01" "Heritage Textile Mill")]
    (is (store/plant-registered? st "mill-01"))
    (is (not (store/plant-verified? st "mill-01")))))

(deftest plant-verification
  "Registered plants can be verified."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-02" "Industrial Mill")
        st (store/verify-plant! st "mill-02")]
    (is (store/plant-registered? st "mill-02"))
    (is (store/plant-verified? st "mill-02"))))

(deftest unregistered-plant-not-verified
  "Unregistered plants are never verified."
  (let [st (store/mem-store)]
    (is (not (store/plant-registered? st "unknown")))
    (is (not (store/plant-verified? st "unknown")))))

(deftest batch-registration
  "Batches can be registered to plants."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-03" "Test Mill")
        st (store/verify-plant! st "mill-03")
        st (store/register-batch! st "batch-001" "mill-03" "cotton" 100.0)]
    (is (store/batch-registered? st "batch-001"))
    (is (not (store/batch-verified? st "batch-001")))))

(deftest batch-verification
  "Batches can be verified."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-04" "Test Mill")
        st (store/verify-plant! st "mill-04")
        st (store/register-batch! st "batch-002" "mill-04" "wool" 50.0)
        st (store/verify-batch! st "batch-002")]
    (is (store/batch-registered? st "batch-002"))
    (is (store/batch-verified? st "batch-002"))))

(deftest get-batch
  "Batch records can be retrieved."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-05" "Test Mill")
        st (store/verify-plant! st "mill-05")
        st (store/register-batch! st "batch-003" "mill-05" "linen" 75.0)
        batch (store/get-batch st "batch-003")]
    (is (= "batch-003" (:id batch)))
    (is (= "linen" (:fiber-type batch)))
    (is (= 75.0 (:weight batch)))))

(deftest maintenance-logging
  "Maintenance records can be logged."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-06" "Test Mill")
        st (store/verify-plant! st "mill-06")
        st (store/log-maintenance! st "mill-06" "spindle-bearing-replacement" "2026-07-14")]
    (is (> (count (:maintenance-records st)) 0))))

(deftest quality-flag-logging
  "Quality issues can be flagged and escalated."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-07" "Test Mill")
        st (store/verify-plant! st "mill-07")
        st (store/register-batch! st "batch-004" "mill-07" "synthetic" 60.0)
        st (store/verify-batch! st "batch-004")
        st (store/flag-quality-issue! st "batch-004" "contamination" "high")]
    (is (store/has-quality-flags? st "batch-004"))))

(deftest quality-flags-tracking
  "Quality flags are tracked per batch."
  (let [st (store/mem-store)
        st (store/register-plant! st "mill-08" "Test Mill")
        st (store/verify-plant! st "mill-08")
        st (store/register-batch! st "batch-005" "mill-08" "cotton" 100.0)
        st (store/verify-batch! st "batch-005")
        st (store/flag-quality-issue! st "batch-005" "evenness-defect" "medium")
        st (store/flag-quality-issue! st "batch-005" "contamination" "high")]
    (is (store/has-quality-flags? st "batch-005"))
    (is (= 2 (count (:quality-flags st))))))
