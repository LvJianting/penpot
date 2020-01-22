;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2019 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.util.transit
  (:require
   [cognitect.transit :as t]
   [clojure.java.io :as io]
   [uxbox.util.time :as dt]
   [uxbox.util.data :as data])
  (:import
   io.vertx.core.buffer.Buffer
   java.io.ByteArrayInputStream
   java.io.ByteArrayOutputStream
   java.io.File))

;; --- Handlers

(def ^:private file-write-handler
  (t/write-handler
   (constantly "file")
   (fn [v] (str v))))

(def +read-handlers+ dt/+read-handlers+)
(def +write-handlers+
  (assoc dt/+write-handlers+
         File file-write-handler))

;; --- Low-Level Api

(defn reader
  ([istream]
   (reader istream nil))
  ([istream {:keys [type] :or {type :json}}]
   (t/reader istream type {:handlers +read-handlers+})))

(defn read!
  "Read value from streamed transit reader."
  [reader]
  (t/read reader))

(defn writer
  ([ostream]
   (writer ostream nil))
  ([ostream {:keys [type] :or {type :json}}]
   (t/writer ostream type {:handlers +write-handlers+})))

(defn write!
  [writer data]
  (t/write writer data))

;; --- High-Level Api

(defn decode
  ([data]
   (decode data nil))
  ([data opts]
   (with-open [input (ByteArrayInputStream. ^bytes data)]
     (read! (reader input opts)))))

(defn encode
  ([data]
   (encode data nil))
  ([data opts]
   (with-open [out (ByteArrayOutputStream.)]
     (let [w (writer out opts)]
       (write! w data)
       (.toByteArray out)))))

;; --- Helpers

(defn str->bytes
  "Convert string to byte array."
  ([^String s]
   (str->bytes s "UTF-8"))
  ([^String s, ^String encoding]
   (.getBytes s encoding)))

(defn bytes->str
  "Convert byte array to String."
  ([^bytes data]
   (bytes->str data "UTF-8"))
  ([^bytes data, ^String encoding]
   (String. data encoding)))

(defn bytes->buffer
  [^bytes data]
  (Buffer/buffer data))

(defn buffer->bytes
  [^Buffer data]
  (.getBytes data))


