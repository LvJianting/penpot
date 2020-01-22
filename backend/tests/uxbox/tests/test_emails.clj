;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2019 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.tests.test-emails
  (:require
   [clojure.test :as t]
   [promesa.core :as p]
   [mockery.core :refer [with-mock]]
   [uxbox.db :as db]
   [uxbox.emails :as emails]
   [uxbox.tests.helpers :as th]))

(t/use-fixtures :once th/state-init)
(t/use-fixtures :each th/database-reset)

(t/deftest register-email-rendering
  (let [result (emails/render emails/register {:to "example@uxbox.io" :name "foo"})]
    (t/is (map? result))
    (t/is (contains? result :subject))
    (t/is (contains? result :body))
    (t/is (contains? result :to))
    (t/is (contains? result :reply-to))
    (t/is (vector? (:body result)))))

;; (t/deftest email-sending-and-sendmail-job
;;   (let [res @(emails/send! emails/register {:to "example@uxbox.io" :name "foo"})]
;;     (t/is (nil? res)))
;;   (with-mock mock
;;     {:target 'uxbox.jobs.sendmail/impl-sendmail
;;      :return (p/resolved nil)}

;;     (let [res @(uxbox.jobs.sendmail/send-emails {})]
;;       (t/is (= 1 res))
;;       (t/is (:called? @mock))
;;       (t/is (= 1 (:call-count @mock))))

;;     (let [res @(uxbox.jobs.sendmail/send-emails {})]
;;       (t/is (= 0 res))
;;       (t/is (:called? @mock))
;;       (t/is (= 1 (:call-count @mock))))))

