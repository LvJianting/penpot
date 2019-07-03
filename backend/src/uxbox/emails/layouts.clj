;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2016 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.emails.layouts
  (:require [uxbox.media :as md]))

(def default-embedded-styles
  "/* GLOBAL */
   * {
     margin:0;
     padding:0;
     font-family: Arial, sans-serif;
     font-size: 100%;
     line-height: 1.6;
   }

   img {
     max-width: 100%;
     width: 100%;
   }

   .img-header {
     border-top-left-radius: 5px;
     border-top-right-radius: 5px;
   }

   body {
     -webkit-font-smoothing:antialiased;
     -webkit-text-size-adjust:none;
     width: 100%!important;
     height: 100%;
   }

   /* ELEMENTS */
   a {
     color: #78dbbe;
     text-decoration:none;
     font-weight: bold;
   }

   .btn-primary {
     text-decoration:none;
     color: #fff;
     background-color: #78dbbe;
     padding: 10px 30px;
     font-weight: bold;
     margin: 20px 0;
     text-align: center;
     cursor: pointer;
     display: inline-block;
     border-radius: 4px;
   }

   .btn-primary:hover {
     color: #FFF;
     background-color: #8eefcf;
   }

   .last {
     margin-bottom: 0;
   }

   .first{
     margin-top: 0;
   }

   .logo {
     background-color: #f6f6f6;
     padding: 10px;
     text-align: center;
     padding-bottom: 25px;
   }
   .logo h2 {
     color: #777;
     font-size: 20px;
     font-weight: bold;
     margin-top: 15px;
   }
   .logo img {
     max-width: 150px;
   }

   /* BODY */
   table.body-wrap {
     width: 100%;
     padding: 20px;
   }

   table.body-wrap .container{
     border-radius: 5px;
     color: #ababab;
   }


   /* FOOTER */
   table.footer-wrap {
     width: 100%;
     clear:both!important;
   }

   .footer-wrap .container p {
     font-size: 12px;
     color:#666;

   }

   table.footer-wrap a{
     color: #999;
   }


   /* TYPOGRAPHY */
   h1,h2,h3{
     font-family: Arial, sans-serif;
     line-height: 1.1;
     margin-bottom:15px;
     color:#000;
     margin: 40px 0 10px;
     line-height: 1.2;
     font-weight:200;
   }

   h1 {
     color: #777;
     font-size: 28px;
     font-weight: bold;
   }
   h2 {
     font-size: 24px;
   }
   h3 {
     font-size: 18px;
   }

   p, ul {
     margin-bottom: 10px;
     font-weight: normal;
   }

   ul li {
     margin-left:5px;
     list-style-position: inside;
   }

   /* RESPONSIVE */

   /* Set a max-width, and make it display as block so it will automatically stretch to that width, but will also shrink down on a phone or something */
   .container {
     display: block !important;
     max-width: 620px !important;
     margin: 0 auto !important; /* makes it centered */
     clear: both !important;
   }

   /* This should also be a block element, so that it will fill 100% of the .container */
   .content {
     padding: 20px;
     max-width: 620px;
     margin: 0 auto;
     display: block;
   }

   /* Let's make sure tables in the content area are 100% wide */
   .content table {
     width: 100%;
   }")

(defn- default-html
  [body context]
  [:html
   [:head
    [:meta {:http-equiv "Content-Type"
            :content "text/html; charset=UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width"}]
    [:title "title"]
    [:style default-embedded-styles]]
   [:body {:bgcolor "#f6f6f6"
           :cz-shortcut-listen "true"}
    [:table.body-wrap
     [:tbody
      [:tr
       [:td]
       [:td.container {:bgcolor "#FFFFFF"}
        [:div.logo
         [:img {:src (md/resolve-asset "images/email/logo.png")
                :alt "UXBOX"}]]
        body]
       [:td]]]]
    [:table.footer-wrap
     [:tbody
      [:tr
       [:td]
       [:td.container
        [:div.content
         [:table
          [:tbody
           [:tr
            [:td
             [:div {:style "text-align: center;"}
              [:a {:href "https://twitter.com/uxboxtool" :target "_blank"}
               [:img {:style "display: inline-block; width: 25px; margin-right: 5px;"
                      :src (md/resolve-asset "images/email/twitter.png")}]]
              [:a {:href "https://github.com/uxbox" :target "_blank"}
               [:img {:style "display: inline-block; width: 25px; margin-right: 5px;"
                      :src (md/resolve-asset "images/email/github.png")}]]
              [:a {:href "https://tree.taiga.io/project/uxbox" :target "_blank"}
               [:img {:style "display: inline-block; width: 25px; margin-right: 5px;"
                      :src (md/resolve-asset "images/email/taiga.png")}]]
              [:a {:href "#" :target "_blank"}
               [:img {:style "display: inline-block; width: 25px; margin-right: 5px;"
                      :src (md/resolve-asset "images/email/linkedin.png")}]]]]]
           [:tr
            [:td {:align "center"}
             [:p
              [:span "Sent from UXBOX | "]
              [:a {:href "#" :target "_blank"}
               [:unsubscribe "Email preferences"]]]]]]]]]
       [:td]]]]]])

(defn default-text
  [body context]
  body)

(def default
  "Default layout instance."
  {:text/html default-html
   :text/plain default-text})
