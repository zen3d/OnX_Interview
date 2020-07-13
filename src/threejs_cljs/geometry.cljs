(ns threejs-cljs.geometry)

(defrecord Point [x y])

(defrecord Rect [lo hi])

(defn make-rect [x-lo y-lo x-hi y-hi]
  (Rect. (Point. x-lo y-lo) (Point. x-hi y-hi))
)

(defn make-rect-lines [rect]
  (let
    [
      x-lo (:x (:lo rect))
      y-lo (:y (:lo rect))
      x-hi (:x (:hi rect))
      y-hi (:y (:hi rect))
    ]
    [
      [x-lo y-lo] [x-lo y-hi]
      [x-lo y-hi] [x-hi y-hi]
      [x-hi y-hi] [x-hi y-lo]
      [x-hi y-lo] [x-lo y-lo]
    ]
  )
)

(defrecord Vector [x y z])
