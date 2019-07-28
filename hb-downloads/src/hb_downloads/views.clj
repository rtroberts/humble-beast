(ns hb-downloads.views
  (:require [hiccup.core :as h]
            [hiccup.page :as p]
            [hiccup.form :as f]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [hb-downloads.database :as database]
            [hb-downloads.mailing-list :as mailing-list]
            [hb-downloads.url-handler :as url-handler]
            )
  )


(defn javascript-tag
  "Wrap the supplied javascript up in script tags and a CDATA section."
  [script]
  [:script {:type "text/javascript"}
   (str "//<![CDATA[\n" script "\n//]]>")])

(defn header
  [src href]
  (h/html
    [:header {:class "header"}
     [:div {:class "logo-container"}
     [:a {:href href}
      [:img {:src src
             :class "logo"}]]]]))


(defn footer
  [conf]
  (h/html
    [:footer 
     [:div {:class "siteInfo"}
      [:span (:city (:footer conf))
       [:a {:href (str "mailto:" (:email (:footer conf)))
            :class "linkText"}
        [:span (:email (:footer conf))]]]
      [:p {:class "rights"}
       [:span (:copyright (:footer conf))]]]]))

(def fonts
  "<link href='https://fonts.googleapis.com/css?family=Roboto|Montserrat|Ubuntu:400,300,300italic' rel='stylesheet' type='text/css'>"
  )

(defn favicon [conf]
  (let [href (:favicon (:style conf))]
    (str "<link rel=\"icon\" href=\"/" href "\" type=\"image/x-icon\"/>
  <link rel=\"shortcut icon\" href=\"/" href "\" type=\"image/x-icon\"/>")))

(def download-javascript "var lb = document.getElementById('album-download').value;
                         console.log(lb);
                         var a = document.createElement('a');
                         a.setAttribute('download', lb);
                         a.target = '_blank';
                         a.href = lb;
                         a.style.display = 'none';
                         document.body.appendChild(a);
                         a.click();")

(defn donation-button
  [conf]
  [:iframe {:id "donate-iframe"
  ; TODO - turn scrolling NO back on once HB uses the right iframe.
  ;:scrolling "no"
  :src (:iframe (:donation conf))}])



(defn layout [config title & content]
  (h/html
    [:div {:class "everytainer"}
     [:head 
      (p/include-js "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.2/angular.min.js")
      (p/include-js "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.2/angular-sanitize.js")
      (p/include-js "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.2/angular-cookies.js")		
      (p/include-css "/css/bootstrap-social.css")
      (p/include-css "/css/font-awesome.min.css")
      (p/include-css (:css (:style config)))
      fonts
      (favicon config)
      [:title title]]
     [:body (header (:site-logo (:style config)) (:site-link (:style config)))
      [:div {:class "content"} content]
      (footer config)
      ]]))


(defn email-page [conf]
  (layout conf 
          "Check your email!"
          [:br]
          [:br]
          [:br]
          [:h1 "We're prepping your download. Check your email for the link!"]
          ))

(defn mobile-page [conf]
  (layout conf
          "Browser unsupported"
          [:br]
          [:h1 "We currently don't support mobile downloads."]
          [:p "You can use the same link to try again on a desktop browser."]
          ))


(defn album-info 
  "Builds the HTML for the album image and blurb"
  [conf album]
  [:div {:class "album-art"}
   [:img {:src (str (:asset-root conf) "/img/" album ".jpg")}]]
  )

(defn already-retrieved [conf site displaystr]
  (layout conf 
          "Key has been used"
          [:br]
          [:h1 displaystr]
          [:p "If you have gotten this message in error, try again or contact Humble Beast Support at support@humblebeast.com"]))


(defn not-found [conf]
  (layout conf "404"
          [:h1 "We couldn't find the item you were looking for."]))


(defn thanks-page [conf albuminfo]
  (println "Thanks page got the following album info: " albuminfo)
  (layout conf
    "Thanks for your support!"
    (p/include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css")
    (p/include-js (str (:asset-root conf) "/js/ui-bootstrap-tpls-1.2.5.min.js"))
    (p/include-js (:download-app (:style conf)))         
    [:div {:class "inner-content"}
     [:h1 "Thanks for your support!"]
        [:p "You can support us further by donating or sending links to your friends." ]
        [:p "If your download does not start automatically, "
         [:a {:href (:url albuminfo)} "click here."] ]
     [:div {:class "content-columns"}
      [:div 
        ;[:br]
        [:div {:class "donate"} (donation-button conf)]]
      [:br]
      [:div {:ng-app "lampwick"}
       (anti-forgery-field)
       (f/hidden-field "album" (:readable albuminfo))
       (f/hidden-field "album-download" (:url albuminfo))
       (javascript-tag download-javascript)
       [:ng-include {:src "'/html/friends.html'"}]
       ]]]))


(defn add-downloader [conf canon urld]
  (layout conf
    canon
    (list 
      (p/include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css")
      (p/include-js "/js/ui-bootstrap-tpls-1.2.5.min.js")
      (p/include-js (:download-app (:style conf)))									
      [:div {:class "inner-content"}
       [:h2 canon]
       (album-info conf urld)		  
       [:div {:ng-app "lampwick"}
        (anti-forgery-field)
        (f/hidden-field "album" canon)
        [:ng-include {:src "'/html/download.html'"}]
        ]])))
