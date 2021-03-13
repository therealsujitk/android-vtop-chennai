package tk.therealsuji.vtopchennai;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class CardGenerator {
    static int CARD_ATTENDANCE = 0;
    static int CARD_DIRECTION = 1;
    static int CARD_EXAM = 2;
    static int CARD_FACULTY = 3;
    static int CARD_GRADE_HISTORY_A = 4;
    static int CARD_GRADE_HISTORY_B = 5;
    static int CARD_GRADE = 6;
    static int CARD_HOME = 7;
    static int CARD_MARK = 8;
    static int CARD_MESSAGE = 9;
    static int CARD_PROCTOR_MESSAGE = 10;
    static int CARD_RECEIPT = 11;
    static int CARD_SPOTLIGHT = 12;
    static int CARD_STAFF = 13;
    static int CARD_TIMETABLE = 14;
    Context context;
    int cardType;
    float pixelDensity;

    public CardGenerator(Context context, int cardType) {
        this.context = context;
        this.cardType = cardType;
        this.pixelDensity = context.getResources().getDisplayMetrics().density;
    }

    private TextView generateTextView(String string, float textSize, boolean bold, boolean textEnd) {
        /*
            The text views with proper formatting
         */
        TextView view = new TextView(context);
        TableRow.LayoutParams viewParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );

        if (cardType == CARD_SPOTLIGHT || cardType == CARD_DIRECTION) {
            viewParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1
            );
        }

        view.setTextSize(textSize);

        if (textEnd) {
            viewParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );

            view.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }

        if (bold) {
            view.setTypeface(ResourcesCompat.getFont(context, R.font.rubik), Typeface.BOLD);
        } else {
            view.setTypeface(ResourcesCompat.getFont(context, R.font.rubik));
        }

        if (cardType == CARD_EXAM || cardType == CARD_GRADE_HISTORY_B || cardType == CARD_MARK) {
            viewParams.setMargins(0, (int) (3 * pixelDensity), 0, (int) (3 * pixelDensity));
        } else {
            viewParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
        }

        view.setText(string);
        view.setTextColor(context.getColor(R.color.colorPrimary));
        view.setLayoutParams(viewParams);

        return view;
    }

    /*
        This method is public because it is used by the HomeActivity
     */
    public LinearLayout generateInnerBlock(String startString, String endString, boolean horizontal, boolean header) {  // header is to check if it is the top layout or the bottom
        /*
            The innerBlock to hold the start / top and end / bottom strings
         */
        LinearLayout innerBlock = new LinearLayout(context);
        LinearLayout.LayoutParams innerBlockParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        innerBlock.setLayoutParams(innerBlockParams);
        if (horizontal) {
            innerBlock.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            innerBlock.setOrientation(LinearLayout.VERTICAL);
        }

        float textSize = 16;
        boolean boldStart = false;
        boolean boldEnd = false;

        if (header && (cardType == CARD_ATTENDANCE || cardType == CARD_DIRECTION || cardType == CARD_GRADE_HISTORY_A || cardType == CARD_HOME || cardType == CARD_STAFF || cardType == CARD_TIMETABLE)) {
            textSize = 20;
        }

        if (cardType == CARD_ATTENDANCE || cardType == CARD_EXAM || cardType == CARD_GRADE_HISTORY_B || cardType == CARD_MARK) {
            boldEnd = true;
        } else if (header && (cardType == CARD_DIRECTION || cardType == CARD_GRADE_HISTORY_A || cardType == CARD_STAFF || cardType == CARD_TIMETABLE)) {
            boldStart = true;
        } else if (cardType == CARD_HOME) {
            innerBlockParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            innerBlock.setLayoutParams(innerBlockParams);

            if (header) {
                boldStart = true;
                boldEnd = true;

                innerBlock.setPadding((int) (20 * pixelDensity), (int) (-5 * pixelDensity), (int) (20 * pixelDensity), 0);
            } else {
                innerBlock.setPadding((int) (20 * pixelDensity), 0, (int) (20 * pixelDensity), (int) (15 * pixelDensity));
            }
        }

        /*
            The start/top view
         */
        TextView view = generateTextView(startString, textSize, boldStart, false);
        innerBlock.addView(view); // Adding the start view to the innerBlock

        /*
            The end/bottom view
         */
        boolean textEnd = true;
        if (!horizontal) {
            textEnd = false;
            textSize = 16;
        }
        view = generateTextView(endString, textSize, boldEnd, textEnd);
        innerBlock.addView(view); // Adding the end view to the innerBlock

        return innerBlock;
    }

    public LinearLayout generateCard(String... strings) {
        /*
            The outer block
         */
        LinearLayout card = new LinearLayout(context);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMarginStart((int) (20 * pixelDensity));
        cardParams.setMarginEnd((int) (20 * pixelDensity));
        cardParams.setMargins(0, (int) (5 * pixelDensity), 0, (int) (5 * pixelDensity));
        card.setPadding((int) (20 * pixelDensity), (int) (15 * pixelDensity), (int) (20 * pixelDensity), (int) (15 * pixelDensity));
        card.setLayoutParams(cardParams);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_card));
        card.setOrientation(LinearLayout.VERTICAL);

        if (cardType == CARD_ATTENDANCE || cardType == CARD_TIMETABLE) {
            card.addView(generateInnerBlock(strings[0], strings[1], true, true));
            card.addView(generateInnerBlock(strings[2], strings[3], true, false));
        } else if (cardType == CARD_MESSAGE || cardType == CARD_PROCTOR_MESSAGE || cardType == CARD_FACULTY || cardType == CARD_RECEIPT) {
            float textSize = 16;
            if (cardType == CARD_FACULTY || cardType == CARD_RECEIPT) {
                textSize = 20;
            }
            card.addView(generateTextView(strings[0], textSize, true, false));
            card.addView(generateInnerBlock(strings[1], strings[2], true, false));
        } else if (cardType == CARD_STAFF) {
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setPadding(0, 0, 0, 0);
            LinearLayout innerBlock = generateInnerBlock(strings[1], strings[0], false, true);
            innerBlock.setPadding((int) (20 * pixelDensity), (int) (15 * pixelDensity), (int) (20 * pixelDensity), (int) (15 * pixelDensity));
            card.addView(innerBlock);

            String key = strings[0].toLowerCase();
            if (key.contains("email")) {
                innerBlock.setPadding((int) (20 * pixelDensity), (int) (15 * pixelDensity), 0, (int) (15 * pixelDensity));
                LinkButtonGenerator myEmail = new LinkButtonGenerator(context);
                card.addView(myEmail.generateButton(strings[1], LinkButtonGenerator.LINK_EMAIL));
            } else if (key.contains("mobile") || key.contains("phone")) {
                innerBlock.setPadding((int) (20 * pixelDensity), (int) (15 * pixelDensity), 0, (int) (15 * pixelDensity));
                LinkButtonGenerator myPhone = new LinkButtonGenerator(context);
                card.addView(myPhone.generateButton(strings[1], LinkButtonGenerator.LINK_CALL));
            }
        } else if (cardType == CARD_SPOTLIGHT) {
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setPadding(0, 0, 0, 0);
            TextView announcementView = generateTextView(strings[0], 16, true, false);
            announcementView.setPadding((int) (20 * pixelDensity), (int) (15 * pixelDensity), (int) (20 * pixelDensity), (int) (15 * pixelDensity));
            card.addView(announcementView);

            if (!strings[1].equals("null")) {
                announcementView.setPadding((int) (20 * pixelDensity), (int) (15 * pixelDensity), 0, (int) (15 * pixelDensity));
            }
        } else if (cardType == CARD_DIRECTION) {
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setPadding(0, 0, 0, 0);

            if (strings[1].equals("")) {
                TextView locationView = generateTextView(strings[0], 20, true, false);
                locationView.setPadding((int) (20 * pixelDensity), (int) (15 * pixelDensity), 0, (int) (15 * pixelDensity));
                card.addView(locationView);
            } else {
                LinearLayout innerBlock = generateInnerBlock(strings[0], strings[1], false, true);
                innerBlock.setPadding((int) (20 * pixelDensity), (int) (15 * pixelDensity), 0, (int) (15 * pixelDensity));
                card.addView(innerBlock);
            }
        } else if (cardType == CARD_EXAM) {
            card.setPadding((int) (20 * pixelDensity), (int) (17 * pixelDensity), (int) (20 * pixelDensity), (int) (17 * pixelDensity));

            card.addView(generateTextView(strings[0], 20, true, false));
            card.addView(generateTextView(strings[1], 16, true, false));

            String[] titles = {context.getString(R.string.slot), context.getString(R.string.date), context.getString(R.string.reporting), context.getString(R.string.timings), context.getString(R.string.venue), context.getString(R.string.location), context.getString(R.string.seat)};

            for (int i = 2; i < strings.length; ++i) {
                if (strings[i].equals("") || strings[i].equals("-")) {
                    continue;
                }

                card.addView(generateInnerBlock(titles[i - 2], strings[i], true, false));
            }
        } else if (cardType == CARD_MARK) {
            card.setPadding((int) (20 * pixelDensity), (int) (17 * pixelDensity), (int) (20 * pixelDensity), (int) (17 * pixelDensity));

            card.addView(generateTextView(strings[0], 20, true, false));

            String[] titles = {context.getString(R.string.type), context.getString(R.string.score), context.getString(R.string.weightage), context.getString(R.string.average), context.getString(R.string.status)};
            for (int i = 1; i < strings.length; ++i) {
                if (strings[i].equals("") || strings[i].equals("-")) {
                    continue;
                }

                card.addView(generateInnerBlock(titles[i - 1], strings[i], true, false));
            }
        } else if (cardType == CARD_GRADE_HISTORY_A) {
            card.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout innerBlock = generateInnerBlock(strings[1], strings[0], false, true);
            card.addView(innerBlock);
        } else if (cardType == CARD_GRADE_HISTORY_B) {
            card.setPadding((int) (20 * pixelDensity), (int) (17 * pixelDensity), (int) (20 * pixelDensity), (int) (17 * pixelDensity));

            card.addView(generateTextView(strings[0], 20, true, false));
            card.addView(generateTextView(strings[1], 16, true, false));

            String[] titles = {context.getString(R.string.credits), context.getString(R.string.grade)};
            for (int i = 2; i < strings.length; ++i) {
                card.addView(generateInnerBlock(titles[i - 2], strings[i], true, false));
            }
        }

        return card;
    }
}
