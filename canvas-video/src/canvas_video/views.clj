(ns canvas-video.views
  (:require [hiccup.core :as h]
            [hiccup.page :as p]
            [hiccup.form :as f]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            )
  )


(defn javascript-tag
  "Wrap the supplied javascript up in script tags and a CDATA section."
  [script]
  [:script {:type "text/javascript"}
   (str "//<![CDATA[\n" script "\n//]]>")])

(defn header
  []
  (h/html
    [:header {:class "header"}
     [:div {:class "container"}
      [:img {:src "/img/hb-logo.png"
             :href "humblebeast.com"
             :class "logo"}]]]))


(defn footer
  []
  (h/html
    [:footer 
     [:div {:class "siteInfo"}
      [:span "Portland, OR - "
       [:a {:href "mailto:info@humblebeast.com"
            :class "linkText"}
        [:span "info@humblebeast.com"]]]
      [:p {:class "rights"}
       [:span "Copyright 2014 © Humble Beast Records. All Rights Reserved."]]]]))

(def fonts
  "<link href='https://fonts.googleapis.com/css?family=Roboto|Montserrat|Ubuntu:400,300,300italic' rel='stylesheet' type='text/css'>"
  )

(def favicon
  "<link rel=\"icon\" href=\"/favicon.ico\" type=\"image/x-icon\"/>
  <link rel=\"shortcut icon\" href=\"/favicon.ico\" type=\"image/x-icon\"/>" )



(defn layout [title & content]
  (h/html
    [:div {:class "everytainer"}
     [:head 
      (p/include-js "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.2/angular.min.js")
      (p/include-js "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.2/angular-sanitize.js")
      (p/include-js "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.2/angular-cookies.js")		
      ;(p/include-css "")
      (p/include-css "/css/bootstrap-social.css")
      (p/include-css "/css/font-awesome.min.css")
      (p/include-css "/css/hb-global.css")
      fonts
      favicon
      [:title title]]
     [:body (header) 
      [:div {:class "content"} content]
      (footer)
      ]]
    ))


(defn not-found []
  (layout "404"
          [:h1 "404 - Not Found"]))


(defn donation-button
  []
  [:div {:id "mcfgjtx14khb5"}
   [:a {:href "https://app.moonclerk.com/pay/fgjtx14khb5"} [:span "Donate"]]
   [:script {:type "text/javascript"}
    (str "var mcfgjtx14khb5;(function(d,t) {var s=d.createElement(t),opts={'checkoutToken':'fgjtx14khb5','width':'100%'};s.src='https://d2l7e0y6ygya2s.cloudfront.net/assets/embed.js';s.onload=s.onreadystatechange = function() {var rs=this.readyState;if(rs) if(rs!='complete') if(rs!='loaded') return;try {mcfgjtx14khb5=new MoonclerkEmbed(opts);mcfgjtx14khb5.display();} catch(e){}};var scr=d.getElementsByTagName(t)[0];scr.parentNode.insertBefore(s,scr);})(document,'script');")
    ]
   ])


(defn video []
  (layout 
    "The Canvas Conference"
    (list 
      (p/include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css")
      (p/include-js "/js/ui-bootstrap-tpls-1.2.5.min.js")
      (p/include-js "/js/canvas-video.js")									
      [:div {:class "inner-content"}
       [:h2 "The Canvas Conference"]
       [:div {:ng-app "lampwick"}
        (anti-forgery-field)
        (f/hidden-field "album" "The Canvas Conference")
        [:ng-include {:src "'/html/download.html'"}]]
      [:br]
      [:p "We rely on your generosity to support our own. Thanks for watching."]
      [:br]
      [:div {:class "donate"} (donation-button)]]
      )))
