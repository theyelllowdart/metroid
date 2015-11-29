package com.example.aaron.metandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Random;

public class DebugMapView extends ImageView {

    private final ArrayList<GalleryViewRect> rects;
    private final ArrayList<Paint> paints;

    public DebugMapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        rects = Lists.newArrayList(
                new GalleryViewRect(305, 329, 159, 399, 245, density),
                new GalleryViewRect(520, 329, 138, 399, 157, density),

                new GalleryViewRect(523, 306, 114, 326, 130, density),
                new GalleryViewRect(522, 306, 133, 327, 156, density),
                new GalleryViewRect(547, 306, 159, 314, 172, density),
                new GalleryViewRect(546, 306, 175, 314, 186, density),
                new GalleryViewRect(545, 316, 160, 327, 186, density),
                new GalleryViewRect(544, 284, 190, 327, 212, density),
                new GalleryViewRect(543, 306, 217, 327, 228, density),
                new GalleryViewRect(542, 306, 232, 327, 244, density),
                new GalleryViewRect(540, 306, 247, 316, 266, density),
                new GalleryViewRect(541, 317, 247, 327, 266, density),
                new GalleryViewRect(537, 306, 269, 326, 288, density),
                new GalleryViewRect(536, 306, 291, 340, 309, density),
                new GalleryViewRect(534, 306, 211, 340, 361, density),

                new GalleryViewRect(525, 272, 114, 303, 135, density),
                new GalleryViewRect(524, 261, 138, 303, 156, density),
                new GalleryViewRect(530, 272, 194, 281, 209, density),
                new GalleryViewRect(539, 282, 249, 303, 266, density),
                new GalleryViewRect(538, 282, 269, 303, 288, density),


                new GalleryViewRect(526, 240, 114, 270, 134, density),
                new GalleryViewRect(527, 240, 137, 260, 158, density),
                new GalleryViewRect(528, 240, 160, 270, 191, density),
                new GalleryViewRect(529, 240, 193, 270, 208, density),
                new GalleryViewRect(531, 240, 212, 270, 233, density),
                new GalleryViewRect(532, 240, 237, 270, 245, density),
                new GalleryViewRect(533, 240, 249, 280, 288, density),

                new GalleryViewRect(548, 208, 111, 233, 293, density),
                new GalleryViewRect(549, 198, 111, 207, 293, density),

                new GalleryViewRect(553, 176, 138, 196, 186, density),
                new GalleryViewRect(552, 176, 190, 196, 214, density),
                new GalleryViewRect(551, 176, 217, 196, 266, density),

                new GalleryViewRect(554, 156, 136, 174, 191, density),
                new GalleryViewRect(555, 144, 193, 174, 211, density),
                new GalleryViewRect(556, 144, 214, 174, 266, density),
                new GalleryViewRect(550, 144, 268, 200, 284, density),

                new GalleryViewRect(905, 45, 57, 73, 83, density),
                new GalleryViewRect(913, 43, 85, 118, 106, density),
                new GalleryViewRect(909, 55, 162, 95, 184, density),

                new GalleryViewRect(906, 77, 57, 105, 83, density),

                new GalleryViewRect(907, 109, 57, 127, 83, density),
                new GalleryViewRect(912, 120, 83, 139, 107, density),
                new GalleryViewRect(911, 100, 111, 129, 131, density),
                new GalleryViewRect(910, 100, 133, 129, 157, density),
                new GalleryViewRect(908, 100, 160, 129, 184, density),


                new GalleryViewRect(904, 24, 57, 42, 83, density),
                new GalleryViewRect(903, 12, 84, 40, 106, density),
                new GalleryViewRect(902, 12, 109, 52, 132, density),
                new GalleryViewRect(901, 12, 135, 52, 156, density),
                new GalleryViewRect(900, 12, 161, 54, 183, density),


                new GalleryViewRect(199, 154, 294, 218, 370, density),


                new GalleryViewRect(358, 95, 210, 141, 266, density),
                new GalleryViewRect(359, 95, 269, 124, 288, density),
                new GalleryViewRect(350, 95, 288, 141, 344, density),
                new GalleryViewRect(351, 95, 347, 141, 361, density),

                new GalleryViewRect(357, 59, 210, 92, 272, density),
                new GalleryViewRect(352, 59, 275, 92, 359, density),

                new GalleryViewRect(356, 31, 210, 56, 259, density),
                new GalleryViewRect(355, 31, 263, 56, 313, density),
                new GalleryViewRect(353, 31, 331, 56, 359, density),

                new GalleryViewRect(354, 0, 211, 27, 360, density),

                new GalleryViewRect(169, 0, 380, 12, 447, density),
                new GalleryViewRect(168, 0, 451, 16, 447, density),

                new GalleryViewRect(162, 16, 375, 113, 447, density),
                new GalleryViewRect(167, 19, 459, 30, 469, density),
                new GalleryViewRect(166, 32, 452, 58, 476, density),
                new GalleryViewRect(165, 60, 459, 67, 469, density),
                new GalleryViewRect(164, 70, 451, 113, 476, density),

                new GalleryViewRect(161, 115, 376, 139, 397, density),
                new GalleryViewRect(160, 115, 402, 139, 448, density),
                new GalleryViewRect(163, 115, 459, 139, 476, density),

                new GalleryViewRect(159, 143, 372, 173, 397, density),
                new GalleryViewRect(153, 143, 401, 247, 421, density),
                new GalleryViewRect(158, 143, 425, 173, 446, density),

                new GalleryViewRect(157, 176, 375, 213, 397, density),
                new GalleryViewRect(156, 176, 425, 213, 446, density),

                new GalleryViewRect(155, 216, 375, 247, 398, density),
                new GalleryViewRect(154, 216, 425, 247, 446, density),

                new GalleryViewRect(151, 250, 374, 285, 399, density),
                new GalleryViewRect(150, 250, 403, 285, 418, density),
                new GalleryViewRect(152, 250, 423, 285, 446, density),

                new GalleryViewRect(519, 402, 116, 422, 139, density),
                new GalleryViewRect(518, 402, 143, 422, 156, density),
                new GalleryViewRect(512, 402, 159, 418, 185, density),
                new GalleryViewRect(306, 402, 188, 458, 216, density),
                new GalleryViewRect(500, 402, 247, 422, 279, density),
                new GalleryViewRect(501, 408, 280, 416, 289, density),

                new GalleryViewRect(517, 424, 116, 430, 136, density),
                new GalleryViewRect(513, 424, 138, 455, 156, density),
                new GalleryViewRect(511, 420, 159, 434, 185, density),
                new GalleryViewRect(510, 436, 159, 447, 185, density),
                new GalleryViewRect(503, 424, 247, 495, 265, density),
                new GalleryViewRect(502, 424, 269, 439, 288, density),
                new GalleryViewRect(504, 449, 269, 465, 288, density),

                new GalleryViewRect(516, 432, 116, 450, 136, density),

                new GalleryViewRect(515, 452, 116, 490, 134, density),
                new GalleryViewRect(514, 457, 137, 490, 156, density),
                new GalleryViewRect(509, 461, 160, 490, 191, density),
                new GalleryViewRect(307, 461, 194, 490, 210, density),
                new GalleryViewRect(508, 461, 212, 490, 227, density),
                new GalleryViewRect(507, 461, 230, 478, 245, density),
                new GalleryViewRect(506, 481, 230, 491, 245, density),
                new GalleryViewRect(506, 466, 269, 482, 282, density),


                new GalleryViewRect(701, 496, 136, 511, 214, density),
                new GalleryViewRect(700, 513, 136, 600, 214, density),

                new GalleryViewRect(745, 591, 76, 614, 111, density),
                new GalleryViewRect(744, 602, 111, 620, 130, density),
                new GalleryViewRect(724, 610, 134, 627, 152, density),
                new GalleryViewRect(723, 600, 155, 627, 176, density),
                new GalleryViewRect(729, 610, 178, 627, 195, density),
                new GalleryViewRect(730, 602, 178, 608, 191, density),
                new GalleryViewRect(702, 602, 197, 672, 208, density),

                new GalleryViewRect(743, 616, 56, 657, 75, density),
                new GalleryViewRect(746, 621, 76, 650, 131, density),
                new GalleryViewRect(725, 629, 133, 644, 157, density),
                new GalleryViewRect(726, 629, 158, 644, 171, density),
                new GalleryViewRect(727, 629, 175, 644, 178, density),
                new GalleryViewRect(728, 629, 180, 644, 198, density),

                new GalleryViewRect(737, 651, 92, 665, 111, density),
                new GalleryViewRect(731, 661, 151, 673, 198, density),

                new GalleryViewRect(741, 667, 59, 680, 74, density),
                new GalleryViewRect(739, 671, 76, 690, 89, density),
                new GalleryViewRect(736, 667, 91, 714, 111, density),
                new GalleryViewRect(735, 674, 113, 714, 158, density),
                new GalleryViewRect(732, 679, 161, 714, 169, density),
                new GalleryViewRect(733, 679, 172, 699, 185, density),

                new GalleryViewRect(740, 686, 57, 702, 68, density),
                new GalleryViewRect(738, 697, 76, 714, 90, density),
                new GalleryViewRect(734, 701, 172, 714, 190, density),

                new GalleryViewRect(373, 496, 220, 495, 220, density),
                new GalleryViewRect(370, 495, 240, 506, 273, density),
                new GalleryViewRect(377, 495, 274, 535, 295, density),

                new GalleryViewRect(374, 537, 220, 567, 238, density),
                new GalleryViewRect(371, 508, 240, 595, 272, density),
                new GalleryViewRect(378, 537, 274, 567, 295, density),

                new GalleryViewRect(375, 569, 220, 595, 238, density),
                new GalleryViewRect(379, 569, 274, 595, 295, density),

                new GalleryViewRect(376, 597, 220, 617, 238, density),
                new GalleryViewRect(372, 597, 240, 607, 291, density),
                new GalleryViewRect(380, 598, 293, 617, 305, density),

                new GalleryViewRect(138, 443, 375, 476, 395, density),
                new GalleryViewRect(100, 443, 395, 463, 429, density),
                new GalleryViewRect(101, 443, 430, 476, 448, density),

                new GalleryViewRect(137, 478, 374, 501, 393, density),
                new GalleryViewRect(102, 480, 418, 492, 427, density),
                new GalleryViewRect(103, 478, 429, 587, 448, density),

                new GalleryViewRect(104, 589, 429, 614, 448, density),

                new GalleryViewRect(130, 622, 307, 648, 343, density),
                new GalleryViewRect(129, 622, 345, 631, 352, density),
                new GalleryViewRect(131, 622, 352, 745, 369, density),
                new GalleryViewRect(128, 616, 374, 635, 394, density),
                new GalleryViewRect(132, 616, 395, 635, 428, density),
                new GalleryViewRect(107, 616, 429, 635, 448, density),
                new GalleryViewRect(105, 612, 452, 629, 462, density),
                new GalleryViewRect(106, 592, 466, 599, 474, density),

                new GalleryViewRect(123, 637, 372, 694, 387, density),
                new GalleryViewRect(127, 637, 388, 660, 403, density),
                new GalleryViewRect(126, 637, 404, 694, 420, density),
                new GalleryViewRect(109, 637, 422, 702, 428, density),
                new GalleryViewRect(108, 637, 429, 654, 448, density),
                new GalleryViewRect(110, 633, 450, 659, 477, density),

                new GalleryViewRect(125, 661, 387, 670, 404, density),
                new GalleryViewRect(112, 656, 429, 673, 448, density),
                new GalleryViewRect(111, 661, 450, 699, 477, density),

                new GalleryViewRect(124, 671, 388, 694, 402, density),
                new GalleryViewRect(113, 675, 429, 691, 448, density),

                new GalleryViewRect(121, 696, 372, 715, 408, density),
                new GalleryViewRect(122, 696, 410, 715, 420, density),
                new GalleryViewRect(117, 704, 422, 715, 448, density),
                new GalleryViewRect(115, 700, 450, 739, 477, density),

                new GalleryViewRect(120, 717, 372, 736, 377, density),
                new GalleryViewRect(119, 717, 380, 745, 403, density),
                new GalleryViewRect(118, 717, 405, 745, 417, density),
                new GalleryViewRect(116, 717, 419, 745, 448, density),

                new GalleryViewRect(304, 343, 246, 384, 289, density),
                new GalleryViewRect(300, 342, 291, 353, 372, density),
                new GalleryViewRect(303, 353, 291, 373, 319, density),
                new GalleryViewRect(302, 353, 320, 373, 344, density),
                new GalleryViewRect(301, 375, 291, 385, 372, density),






                new GalleryViewRect(960, 294, 68, 309, 85, density),
                new GalleryViewRect(950, 412, 70, 432, 85, density)
        );

        final Random random = new Random(0);
        paints = new ArrayList<>(rects.size());
        for (int i = 0; i < rects.size(); i++) {
            final Paint paint = new Paint();
            paint.setColor(random.nextInt());
            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha(95);
            paints.add(paint);
        }
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < rects.size(); i++) {
            final Paint paint = paints.get(i);
            final GalleryViewRect galleryViewRect = rects.get(i);

            final RectF scaled = galleryViewRect.getScaled();
            final RectF transformed = galleryViewRect.getTransformed();
            getImageMatrix().mapRect(transformed, scaled);
            canvas.drawRect(transformed, paint);
        }
        Path path = new Path();
        final float density = getResources().getDisplayMetrics().density;
        path.moveTo(321 * density, 62 * density);
        path.lineTo(309 * density, 50 * density);
        path.lineTo(318 * density, 41 * density);
        path.lineTo(331 * density, 53 * density);
        path.close();


        Path transformedPath = new Path();
        path.transform(getImageMatrix(), transformedPath);
        canvas.drawPath(transformedPath, paints.get(0));
    }

    private class Polygon {
        private int[] polyY, polyX;
        private int polySides;

        public Polygon(int[] px, int[] py, int ps) {
            polyX = px;
            polyY = py;
            polySides = ps;
        }

        /**
         * Checks if the Polygon contains a point.
         *
         * @param x Point horizontal pos.
         * @param y Point vertical pos.
         * @return Point is in Poly flag.
         * @see "http://alienryderflex.com/polygon/"
         */
        public boolean contains(int x, int y) {
            boolean c = false;
            int i, j = 0;
            for (i = 0, j = polySides - 1; i < polySides; j = i++) {
                if (((polyY[i] > y) != (polyY[j] > y))
                        && (x < (polyX[j] - polyX[i]) * (y - polyY[i]) / (polyY[j] - polyY[i]) + polyX[i]))
                    c = !c;
            }
            return c;
        }
    }
}

