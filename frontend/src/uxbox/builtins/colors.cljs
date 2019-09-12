;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2015-2016 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.builtins.colors
  (:require [uxbox.util.uuid :as uuid]
            [uxbox.util.data :refer [index-by-id]]))

(def collections-list
  [{:name "UXBOX"
    :id #uuid "00000000-0000-0000-0000-000000000001"
    :type :builtin
    :created-at 1
    :colors #{"#78dbbe"
              "#b6dd75"
              "#a599c6"
              "#e6a16f"
              "#de4762"
              "#59b9e2"
              "#ffffff"
              "#000000"
              "#90969d"
              "#D3D3D3"
              "#C0C0C0"
              "#A9A9A9"
              "#DCDCDC"
              "#808080"
              "#696969"}}

   {:name "UXBOX (Light)"
    :id #uuid "00000000-0000-0000-0000-000000000002"
    :type :builtin
    :created-at 2
    :colors #{"#e9eaeb"
              "#a6abb1"
              "#90969d"
              "#d7d9dc"
              "#757a7f"
              "#565a5e"}}

   {:name "UXBOX (Dark)"
    :id #uuid "00000000-0000-0000-0000-000000000003"
    :type :builtin
    :created-at 3
    :colors #{"#2C2C2C"
              "#3d3f40"
              "#181818"
              "#a9adaf"
              "#808386"
              "#4a4e52"
              "#e0e6e9"
              "#8d9496"
              "#4e4f50"
              "#878c8e"}}

   {:name "UXBOX (Blues)"
    :id #uuid "00000000-0000-0000-0000-000000000004"
    :type :builtin
    :created-at 4
    :colors #{"#F0F8FF"
              "#E6E6FA"
              "#B0E0E6"
              "#ADD8E6"
              "#87CEFA"
              "#87CEEB"
              "#00BFFF"
              "#B0C4DE"
              "#1E90FF"
              "#6495ED"
              "#4682B4"
              "#5F9EA0"
              "#7B68EE"
              "#6A5ACD"
              "#483D8B"
              "#4169E1"
              "#0000FF"
              "#0000CD"
              "#00008B"
              "#000080"
              "#191970"
              "#8A2BE2"
              "#4B0082"}}

   ;; https://github.com/twbs/bootstrap
   {:name "Bootstrap"
    :id #uuid "00000000-0000-0000-0000-000000000005"
    :type :builtin
    :created-at 5
    :colors #{"#ffffff"
              "#f8f9fa"
              "#e9ecef"
              "#dee2e6"
              "#ced4da"
              "#adb5bd"
              "#6c757d"
              "#495057"
              "#343a40"
              "#212529"
              "#000000"
              "#007bff"
              "#6610f2"
              "#6f42c1"
              "#e83e8c"
              "#dc3545"
              "#fd7e14"
              "#ffc107"
              "#28a745"
              "#20c997"
              "#17a2b8"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Red)"
    :id #uuid "00000000-0000-0000-0000-000000000006"
    :type :builtin
    :created-at 6
    :colors #{"#ffebee"
              "#ffcdd2"
              "#ef9a9a"
              "#e57373"
              "#ef5350"
              "#f44336"
              "#e53935"
              "#d32f2f"
              "#c62828"
              "#b71c1c"
              "#ff8a80"
              "#ff5252"
              "#ff1744"
              "#d50000"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Pink)"
    :id #uuid "00000000-0000-0000-0000-000000000007"
    :type :builtin
    :created-at 7
    :colors #{"#fce4ec"
              "#f8bbd0"
              "#f48fb1"
              "#f06292"
              "#ec407a"
              "#e91e63"
              "#d81b60"
              "#c2185b"
              "#ad1457"
              "#880e4f"
              "#ff80ab"
              "#ff4081"
              "#f50057"
              "#c51162"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Purple)"
    :id #uuid "00000000-0000-0000-0000-000000000008"
    :type :builtin
    :created-at 8
    :colors #{"#f3e5f5"
              "#e1bee7"
              "#ce93d8"
              "#ba68c8"
              "#ab47bc"
              "#9c27b0"
              "#8e24aa"
              "#7b1fa2"
              "#6a1b9a"
              "#4a148c"
              "#ea80fc"
              "#e040fb"
              "#d500f9"
              "#aa00ff"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Deep Purple)"
    :id #uuid "00000000-0000-0000-0000-000000000009"
    :type :builtin
    :created-at 9
    :colors #{"#ede7f6"
              "#d1c4e9"
              "#b39ddb"
              "#9575cd"
              "#7e57c2"
              "#673ab7"
              "#5e35b1"
              "#512da8"
              "#4527a0"
              "#311b92"
              "#b388ff"
              "#7c4dff"
              "#651fff"
              "#6200ea"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Indigo)"
    :id #uuid "00000000-0000-0000-0000-000000000010"
    :type :builtin
    :created-at 10
    :colors #{"#e8eaf6"
              "#c5cae9"
              "#9fa8da"
              "#7986cb"
              "#5c6bc0"
              "#3f51b5"
              "#3949ab"
              "#303f9f"
              "#283593"
              "#1a237e"
              "#8c9eff"
              "#536dfe"
              "#3d5afe"
              "#304ffe"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Blue)"
    :id #uuid "00000000-0000-0000-0000-000000000011"
    :type :builtin
    :created-at 11
    :colors #{"#e3f2fd"
              "#bbdefb"
              "#90caf9"
              "#64b5f6"
              "#42a5f5"
              "#2196f3"
              "#1e88e5"
              "#1976d2"
              "#1565c0"
              "#0d47a1"
              "#82b1ff"
              "#448aff"
              "#2979ff"
              "#2962ff"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Light Blue)"
    :id #uuid "00000000-0000-0000-0000-000000000012"
    :type :builtin
    :created-at 12
    :colors #{"#e1f5fe"
              "#b3e5fc"
              "#81d4fa"
              "#4fc3f7"
              "#29b6f6"
              "#03a9f4"
              "#039be5"
              "#0288d1"
              "#0277bd"
              "#01579b"
              "#80d8ff"
              "#40c4ff"
              "#00b0ff"
              "#0091ea"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Cyan)"
    :id #uuid "00000000-0000-0000-0000-000000000013"
    :type :builtin
    :created-at 13
    :colors #{"#e0f7fa"
              "#b2ebf2"
              "#80deea"
              "#4dd0e1"
              "#26c6da"
              "#00bcd4"
              "#00acc1"
              "#0097a7"
              "#00838f"
              "#006064"
              "#84ffff"
              "#18ffff"
              "#00e5ff"
              "#00b8d4"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Teal)"
    :id #uuid "00000000-0000-0000-0000-000000000014"
    :type :builtin
    :created-at 14
    :colors #{"#e0f2f1"
              "#b2dfdb"
              "#80cbc4"
              "#4db6ac"
              "#26a69a"
              "#009688"
              "#00897b"
              "#00796b"
              "#00695c"
              "#004d40"
              "#a7ffeb"
              "#64ffda"
              "#1de9b6"
              "#00bfa5"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Green)"
    :id #uuid "00000000-0000-0000-0000-000000000015"
    :type :builtin
    :created-at 15
    :colors #{"#e8f5e9"
              "#c8e6c9"
              "#a5d6a7"
              "#81c784"
              "#66bb6a"
              "#4caf50"
              "#43a047"
              "#388e3c"
              "#2e7d32"
              "#1b5e20"
              "#b9f6ca"
              "#69f0ae"
              "#00e676"
              "#00c853"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Light Green)"
    :id #uuid "00000000-0000-0000-0000-000000000016"
    :type :builtin
    :created-at 16
    :colors #{"#f1f8e9"
              "#dcedc8"
              "#c5e1a5"
              "#aed581"
              "#9ccc65"
              "#8bc34a"
              "#7cb342"
              "#689f38"
              "#558b2f"
              "#33691e"
              "#ccff90"
              "#b2ff59"
              "#76ff03"
              "#64dd17"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Lime)"
    :id #uuid "00000000-0000-0000-0000-000000000017"
    :type :builtin
    :created-at 17
    :colors #{"#f9fbe7"
              "#f0f4c3"
              "#e6ee9c"
              "#dce775"
              "#d4e157"
              "#cddc39"
              "#c0ca33"
              "#afb42b"
              "#9e9d24"
              "#827717"
              "#f4ff81"
              "#eeff41"
              "#c6ff00"
              "#aeea00"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Yellow)"
    :id #uuid "00000000-0000-0000-0000-000000000018"
    :type :builtin
    :created-at 18
    :colors #{"#fffde7"
              "#fff9c4"
              "#fff59d"
              "#fff176"
              "#ffee58"
              "#ffeb3b"
              "#fdd835"
              "#fbc02d"
              "#f9a825"
              "#f57f17"
              "#ffff8d"
              "#ffff00"
              "#ffea00"
              "#ffd600"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Amber)"
    :id #uuid "00000000-0000-0000-0000-000000000019"
    :type :builtin
    :created-at 19
    :colors #{"#fff8e1"
              "#ffecb3"
              "#ffe082"
              "#ffd54f"
              "#ffca28"
              "#ffc107"
              "#ffb300"
              "#ffa000"
              "#ff8f00"
              "#ff6f00"
              "#ffe57f"
              "#ffd740"
              "#ffc400"
              "#ffab00"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Orange)"
    :id #uuid "00000000-0000-0000-0000-000000000020"
    :type :builtin
    :created-at 20
    :colors #{"#fff3e0"
              "#ffe0b2"
              "#ffcc80"
              "#ffb74d"
              "#ffa726"
              "#ff9800"
              "#fb8c00"
              "#f57c00"
              "#ef6c00"
              "#e65100"
              "#ffd180"
              "#ffab40"
              "#ff9100"
              "#ff6d00"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Deep Orange)"
    :id #uuid "00000000-0000-0000-0000-000000000021"
    :type :builtin
    :created-at 21
    :colors #{"#fbe9e7"
              "#ffccbc"
              "#ffab91"
              "#ff8a65"
              "#ff7043"
              "#ff5722"
              "#f4511e"
              "#e64a19"
              "#d84315"
              "#bf360c"
              "#ff9e80"
              "#ff6e40"
              "#ff3d00"
              "#dd2c00"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Brown)"
    :id #uuid "00000000-0000-0000-0000-000000000022"
    :type :builtin
    :created-at 22
    :colors #{"#efebe9"
              "#d7ccc8"
              "#bcaaa4"
              "#a1887f"
              "#8d6e63"
              "#795548"
              "#6d4c41"
              "#5d4037"
              "#4e342e"
              "#3e2723"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Gray)"
    :id #uuid "00000000-0000-0000-0000-000000000023"
    :type :builtin
    :created-at 23
    :colors #{"#fafafa"
              "#f5f5f5"
              "#eeeeee"
              "#e0e0e0"
              "#bdbdbd"
              "#9e9e9e"
              "#757575"
              "#616161"
              "#424242"
              "#212121"}}

   ;; https://material.io/design/color/the-color-system.html#tools-for-picking-colors
   {:name "Material Design (Blue Gray)"
    :id #uuid "00000000-0000-0000-0000-000000000024"
    :type :builtin
    :created-at 24
    :colors #{"#eceff1"
              "#cfd8dc"
              "#b0bec5"
              "#90a4ae"
              "#78909c"
              "#607d8b"
              "#546e7a"
              "#455a64"
              "#37474f"
              "#263238"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (FACEBOOK)"
    :id #uuid "00000000-0000-0000-0000-000000000025"
    :type :builtin
    :created-at 25
    :colors #{"#3B5998"
              "#D8DFEA"
              "#F03D25"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (TWITTER)"
    :id #uuid "00000000-0000-0000-0000-000000000026"
    :type :builtin
    :created-at 26
    :colors #{"#55ACEE"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (WHATSAPP)"
    :id #uuid "00000000-0000-0000-0000-000000000027"
    :type :builtin
    :created-at 27
    :colors #{"#00e676"
              "#1ebea5"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (LINKEDIN)"
    :id #uuid "00000000-0000-0000-0000-000000000028"
    :type :builtin
    :created-at 28
    :colors #{"#0976B4"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (PINTEREST)"
    :id #uuid "00000000-0000-0000-0000-000000000029"
    :type :builtin
    :created-at 29
    :colors #{"#cd1d1f"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (FOURSQUARE)"
    :id #uuid "00000000-0000-0000-0000-000000000030"
    :type :builtin
    :created-at 30
    :colors #{"#0072b1"
              "#0cbadf"
              "#8fd400"
              "#ff7900"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (YOUTUBE)"
    :id #uuid "00000000-0000-0000-0000-000000000031"
    :type :builtin
    :created-at 31
    :colors #{"#CC181E"
              "#110f10"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (INSTAGRAM)"
    :id #uuid "00000000-0000-0000-0000-000000000032"
    :type :builtin
    :created-at 32
    :colors #{"#3F729B"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (FLICKR)"
    :id #uuid "00000000-0000-0000-0000-000000000033"
    :type :builtin
    :created-at 33
    :colors #{"#0063dd"
              "#ff0085"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (VIMEO)"
    :id #uuid "00000000-0000-0000-0000-000000000034"
    :type :builtin
    :created-at 34
    :colors #{"#1ab7ea"
              "#162221"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (TUMBLR)"
    :id #uuid "00000000-0000-0000-0000-000000000035"
    :type :builtin
    :created-at 35
    :colors #{"#35465d"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (STUMBLEUPON)"
    :id #uuid "00000000-0000-0000-0000-000000000036"
    :type :builtin
    :created-at 36
    :colors #{"#EB4924"
              "#333333"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (MYSPACE)"
    :id #uuid "00000000-0000-0000-0000-000000000037"
    :type :builtin
    :created-at 37
    :colors #{"#008DDE"
              "#313131"
              "#1D1D1D"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (DRIBBBLE)"
    :id #uuid "00000000-0000-0000-0000-000000000038"
    :type :builtin
    :created-at 38
    :colors #{"#ea4c89"
              "#444444"
              "#8aba56"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (DAILYMOTION)"
    :id #uuid "00000000-0000-0000-0000-000000000039"
    :type :builtin
    :created-at 39
    :colors #{"#0079b8"
              "#fed417"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (DELICIOUS)"
    :id #uuid "00000000-0000-0000-0000-000000000040"
    :type :builtin
    :created-at 40
    :colors #{"#3274D0"
              "#D3D2D2"
              "#222222"
              "#0B79E5"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (SOUNDCLOUD)"
    :id #uuid "00000000-0000-0000-0000-000000000041"
    :type :builtin
    :created-at 41
    :colors #{"#FF3D00"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (GITHUB)"
    :id #uuid "00000000-0000-0000-0000-000000000042"
    :type :builtin
    :created-at 42
    :colors #{"#171515"
              "#4183c4"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (QUORA)"
    :id #uuid "00000000-0000-0000-0000-000000000043"
    :type :builtin
    :created-at 43
    :colors #{"#BC2016"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (BEHANCE)"
    :id #uuid "00000000-0000-0000-0000-000000000044"
    :type :builtin
    :created-at 44
    :colors #{"#1769FF"
              "#242424"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (VINE)"
    :id #uuid "00000000-0000-0000-0000-000000000045"
    :type :builtin
    :created-at 45
    :colors #{"#00B489"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (VKONTAKTE)"
    :id #uuid "00000000-0000-0000-0000-000000000046"
    :type :builtin
    :created-at 46
    :colors #{"#587fa4"
              "#e9edf1"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (REDDIT)"
    :id #uuid "00000000-0000-0000-0000-000000000047"
    :type :builtin
    :created-at 47
    :colors #{"#F64720"
              "#CEE3F8"}}

   ;; https://github.com/redpik/social-media-colors
   {:name "Social Media (DROPBOX)"
    :id #uuid "00000000-0000-0000-0000-000000000048"
    :type :builtin
    :created-at 48
    :colors #{"#007ee5"
              "#7B8994"
              "#47525D"
              "#3D464D"}}])

(def collections
  (index-by-id collections-list))
