(ns quadtree-viewer.geometry)

(defrecord Point [x y])

(defn add-point [pt0 pt1]
  (Point. (+ (:x pt0) (:x pt1)) (+ (:y pt0) (:y pt1)))
)

(defn sub-point [pt0 pt1]
  (Point. (- (:x pt0) (:x pt1)) (- (:y pt0) (:y pt1)))
)

(defn min-point [point0 point1]
  (let
    [
      x (min (:x point0) (:x point1))
      y (min (:y point0) (:y point1))
    ]
    (Point. x y)
  )
)

(defn max-point [point0 point1]
  (let
    [
      x (max (:x point0) (:x point1))
      y (max (:y point0) (:y point1))
    ]
    (Point. x y)
  )
)

(defn mid-point [point0 point1]
  (let
    [
      x (* 0.5 (+ (:x point0) (:x point1)))
      y (* 0.5 (+ (:y point0) (:y point1)))
    ]
    (Point. x y)
  )
)

(defrecord Rect [lo hi])

(defn make-rect [x-lo y-lo x-hi y-hi]
  (Rect. (Point. x-lo y-lo) (Point. x-hi y-hi))
)

(defn print-rect [rect]
  (pr (:x (:lo rect)) (:y (:lo rect)) (:x (:hi rect)) (:y (:hi rect)))
)

(defn intersect-rect [rect0 rect1]
  (Rect. (max-point (:lo rect0) (:lo rect1)) (min-point (:hi rect0) (:hi rect1)))
)

(defn is-empty-rect [rect]
  (let
    [
      lo-point (:lo rect)
      hi-point (:hi rect)
    ]
    (or (>= (:x lo-point) (:x hi-point)) (>= (:y lo-point) (:y hi-point)))
  )
)

(defn is-intersecting-rect [rect0 rect1]
  (let
    [
      intersection-rect (intersect-rect rect0 rect1)
    ]
    (not (is-empty-rect intersection-rect))
  )
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
