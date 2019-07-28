(ns artist-pages.views
  (:require [hiccup.core :as h]
            [hiccup.page :as p]
            [hiccup.form :as f]
            [clojure.string :as string]
            [artist-pages.rss :as rss]
            )
  )

(defn header
  []
  (h/html
    [:header {:class "header"}
     [:div {:class "container" :style "background-color: black; height: 300px; width:560px"}
      [:img {:src "/img/hb-logo.png"
             :href "humblebeast.com"
             :style "height: 100%; width: 100%"
             :class "logo"}]]]))

(def favicon
  "<link rel=\"icon\" href=\"/favicon.ico\" type=\"image/x-icon\"/>
  <link rel=\"shortcut icon\" href=\"/favicon.ico\" type=\"image/x-icon\"/>" )


(defn footer
  []
  (h/html
    [:script {:src "//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"}]
    (p/include-js "/js/parallax.min.js")
    [:footer 
     [:div {:class "siteInfo"}
      [:p "HUMBLE BEAST"]
      [:span "Portland, OR - "
       [:a {:href "mailto:info@humblebeast.com"
            :class "linkText nostyle"}
        [:span "info@humblebeast.com"]]]
      [:p {:class "rights"}
       [:span "Copyright 2014 © Humble Beast Records. All Rights Reserved."]]]]))


(defn social-icons
  "build HTML for a list of social media icons"
  [artist-map]
  [:div {:class "social-icons"}
   (for [x (:social artist-map)]
     [:span {:class "fa-stack fa-lg"}
      [:a {:href (:link x)}
       [:i {:class (str "fa fa-" (:name x) " fa-stack-1x")}]]]
     )
   ]
  )

(defn top
  "Build the top image scrollax"
  [artist-map]
  [:section {:class "photoContainer top"}
   [:div {:class "parallax-window" :data-parallax "scroll" :data-image-src (:main-image artist-map)}
    [:div {:class "artist-title" :style (if (:invert-header artist-map)
                                          (str "filter: invert(1)")
                                          "")}
     [:img {:class "hb-logo" :src "/img/hb-logo.png"}]
     [:h1 {:class "artist-name"} (:name artist-map)]
     (social-icons artist-map)
     ]
    ]])

(defn bio
  "Build the HTML for the Bio section"
  [artist-map]
  [:section {:class "normal biocontainer"}
   [:div {:class "content"}
    [:h1 "biography"]
    (for [x (:bio artist-map)]
      [:p {:class "artist-bio"} x])
    ]])

(defn news
  "Build the HTML for the Bio section"
  [artist-map]
  [:section {:class "normal greycontainer"}
   [:div {:class "content"}
    [:h1 {:class "white-header"} "NEWS"]
    (let [feed (rss/read-feed artist-map)]
      (if (seq feed)
        [:ul (for [x feed]
               [:li {:class "news-item white-text"}
                [:a {:href (:link x) :class "news-links white-text"} (:title x)] 
                [:div (:description x)
                 ]])]
        [:p {:class "no-news"} "No recent headlines"]))]])


(defn shows
  "Build the HTML for the shows section"
  [artist-map]
  [:section {:class "normal biocontainer"}
   [:div {:class "content"}
    [:div {:class "artist-shows"} 
     [:h1 "Upcoming Shows "]
     [:p "here, here, and here"]
     [:div {:style "text-align:center"}
      [:a {:class "nostyle" :href "http://humblebeast.com/booking"} 
       [:span {:class "book-button"} "BOOK NOW"]]]]]])

(defn albums
  "Build the HTML for the album section"
  [artist-map]
  [:section {:class "normal greycontainer"}
   [:div {:class "content"}
    [:h1 {:class "white-header"} "discography"]
    [:div {:class "disc-contain"}
     (for [x (:albums artist-map)]
       [:a {:href (:link x)}
        [:img {:class "album-img" :src (:src x)}]])
     ]
    ]]) 

(def fonts
  "<link href='https://fonts.googleapis.com/css?family=Roboto|Montserrat|Ubuntu:400,300,300italic' rel='stylesheet' type='text/css'>"
  )

(defn layout [title artist-map & content]
  (h/html
    [:head 
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (p/include-css "/css/hb-artist-global.css")
     ;<link rel="stylesheet" href="path/to/font-awesome/css/font-awesome.min.css">
     (p/include-css "/css/font-awesome.min.css")
     fonts
     favicon
     [:title title]]
    [:body ;(header) 
     (top artist-map)
     (bio artist-map)
     (news artist-map)
     (shows artist-map)
     (albums artist-map)
     (footer)
     ]))


(defn artist
  "Builds an artist page from an artist config map"
  [art-conf]
  (layout (:name art-conf) art-conf))
