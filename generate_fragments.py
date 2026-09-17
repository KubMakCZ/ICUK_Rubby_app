import os

fragments_info = {
    "one": {"title": "isFragmentOne", "extra": ""},
    "two": {"title": "isFragmentTwo", "extra": '<TextView android:layout_width="match_parent" android:layout_height="wrap_content" android:gravity="center" android:text="www.icuk.cz" android:textColor="@color/cb_blue" android:textSize="40sp" app:layout_constraintTop_toBottomOf="@+id/two_text" app:layout_constraintBottom_toTopOf="@+id/two_say"/>'},
    "three": {"title": "isFragmentThree", "extra": '<ImageView android:layout_width="400dp" android:layout_height="150dp" android:src="@drawable/logo_kraj_nove" app:layout_constraintTop_toBottomOf="@+id/three_text" app:layout_constraintBottom_toTopOf="@+id/three_say" app:layout_constraintStart_toStartOf="parent" app:layout_constraintEnd_toEndOf="parent"/>'},
    "four": {"title": "isFragmentFour", "extra": '<LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:gravity="center" app:layout_constraintTop_toBottomOf="@+id/four_text" app:layout_constraintBottom_toTopOf="@+id/four_say"><ImageView android:layout_width="300dp" android:layout_height="100dp" android:src="@drawable/logo_ujep" android:layout_marginBottom="16dp"/><TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="www.ujep.cz" android:textColor="@color/cb_blue" android:textSize="40sp"/></LinearLayout>'},
    "five": {"title": "isFragmentFive", "extra": ""},
    "six": {"title": "isFragmentSix", "extra": ""},
    "seven": {"title": "isFragmentSeven", "extra": ""},
    "eight": {"title": "isFragmentEight", "extra": ""}
}

template = """<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/cb_canvas"
    android:padding="32dp">

    <!-- TITLE -->
    <TextView
        android:id="@+id/{ID}_text"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:gravity="center"
        android:text="@string/{TITLE}"
        android:textColor="@color/cb_ink"
        android:textSize="56sp"
        android:textStyle="bold"
        app:layout_constraintTop_toTopOf="parent" />

    {EXTRA}

    <!-- SECONDARY BUTTON (RESET) -->
    <androidx.appcompat.widget.AppCompatButton
        android:id="@+id/{ID}_button_reset"
        android:layout_width="500dp"
        android:layout_height="120dp"
        android:layout_marginBottom="32dp"
        android:background="@drawable/bg_btn_secondary"
        android:text="@string/reset"
        android:textAllCaps="false"
        android:textColor="@color/cb_ink"
        android:textSize="36sp"
        android:textStyle="bold"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent" />

    <!-- PRIMARY BUTTON (SAY) -->
    <androidx.appcompat.widget.AppCompatButton
        android:id="@+id/{ID}_say"
        android:layout_width="500dp"
        android:layout_height="120dp"
        android:layout_marginBottom="32dp"
        android:background="@drawable/bg_btn_primary"
        android:text="@string/saySomething"
        android:textAllCaps="false"
        android:textColor="@color/white"
        android:textSize="36sp"
        android:textStyle="bold"
        app:layout_constraintBottom_toTopOf="@+id/{ID}_button_reset"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
"""

for frag_id, info in fragments_info.items():
    file_path = f"app/src/main/res/layout/fragment_{frag_id}.xml"
    content = template.replace("{ID}", frag_id).replace("{TITLE}", info["title"]).replace("{EXTRA}", info["extra"])
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Generated {file_path}")
