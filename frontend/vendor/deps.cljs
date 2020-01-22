{:foreign-libs
 [{:file "snapsvg/snap.svg.js"
   :file-min "snapsvg/snap.svg.min.js"
   :provides ["vendor.snapsvg"]}
  {:file "jszip/jszip.js"
   :file-min "jszip/jszip.min.js"
   :provides ["vendor.jszip"]}
  {:file "datefns/datefns.bundle.js"
   :file-min "datefns/datefns.bundle.min.js"
   :provides ["vendor.datefns"]}
  {:file "react-color/react-color.bundle.js"
   :file-min "react-color/react-color.bundle.min.js"
   :requires ["cljsjs.react"]
   :provides ["vendor.react-color"]}
  {:file "randomcolor/randomcolor.bundle.js"
   :file-min "randomcolor/randomcolor.bundle.min.js"
   :provides ["vendor.randomcolor"]}
  {:file "react-dnd/react-dnd.bundle.js"
   :file-min "react-dnd/react-dnd.bundle.min.js"
   :requires ["cljsjs.react"]
   :provides ["vendor.react-dnd"]}]
 :externs ["main.externs.js"
           "snapsvg/externs.js"
           "jszip/externs.js"
           "randomcolor/externs.js"
           "react-color/externs.js"
           "react-dnd/externs.js"
           "datefns/externs.js"]}
