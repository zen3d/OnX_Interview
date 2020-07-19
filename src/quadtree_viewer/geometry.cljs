(ns quadtree-viewer.geometry)

;; Point - a tuple of x,y pairs that represent a 2D point.
(defrecord Point [x y])

;; Add two points.
(defn add-point [pt0 pt1]
  (Point. (+ (:x pt0) (:x pt1)) (+ (:y pt0) (:y pt1)))
)

;; Subtract one point (pt1) from another (pt0).
(defn sub-point [pt0 pt1]
  (Point. (- (:x pt0) (:x pt1)) (- (:y pt0) (:y pt1)))
)

;; Compute the minima of two points.
(defn min-point [point0 point1]
  (let
    [
      x (min (:x point0) (:x point1))
      y (min (:y point0) (:y point1))
    ]
    (Point. x y)
  )
)

;; Compute the maxima of two points.
(defn max-point [point0 point1]
  (let
    [
      x (max (:x point0) (:x point1))
      y (max (:y point0) (:y point1))
    ]
    (Point. x y)
  )
)

;; Compute the midpoint of two points.
(defn mid-point [point0 point1]
  (let
    [
      x (* 0.5 (+ (:x point0) (:x point1)))
      y (* 0.5 (+ (:y point0) (:y point1)))
    ]
    (Point. x y)
  )
)

;; Rect - a tuple of two points that represents a 2D rectangle.
;; INVARIANT: (:x lo) <= (:x hi)
;; INVARIANT: (:y lo) <= (:y hi)
;; If these invariants are not met, the rectangle is invalid/empty.
(defrecord Rect [lo hi])

;; make-rect - convenience function.
(defn make-rect [x-lo y-lo x-hi y-hi]
  (Rect. (Point. x-lo y-lo) (Point. x-hi y-hi))
)

;; print-rect - convenience function.
(defn print-rect [rect]
  (pr (:x (:lo rect)) (:y (:lo rect)) (:x (:hi rect)) (:y (:hi rect)))
)

;; intersect-rect - compute the rectangle that is the intersection of two rectangles.
(defn intersect-rect [rect0 rect1]
  (Rect. (max-point (:lo rect0) (:lo rect1)) (min-point (:hi rect0) (:hi rect1)))
)

;; is-emptry-rect - test if rectangle invariance is not met,
;; implying the rectangle is empty.
(defn is-empty-rect [rect]
  (let
    [
      lo-point (:lo rect)
      hi-point (:hi rect)
    ]
    (or (>= (:x lo-point) (:x hi-point)) (>= (:y lo-point) (:y hi-point)))
  )
)

;; is-intersecting-rect - test if the intersection of two rectangles is non-empty.
(defn is-intersecting-rect [rect0 rect1]
  (let
    [
      intersection-rect (intersect-rect rect0 rect1)
    ]
    (not (is-empty-rect intersection-rect))
  )
)

;; make-rect-lines - make line segments for a rectangle's edges.
;; NOTE: unused by this code; legacy of prototype.
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

;; Vector - a tuple of x,y,z coordinates that represent a 3D point
;; NOTE: unused by this code; legacy of prototype.
(defrecord Vector [x y z])
