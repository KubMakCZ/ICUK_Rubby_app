import glob
import re

for f in glob.glob('app/src/main/res/layout/fragment_*.xml'):
    if f.endswith('fragment_main.xml') or f.endswith('fragment_loading.xml') or f.endswith('fragment_splash.xml'): continue
    
    with open(f, 'r', encoding='utf-8') as file: 
        content = file.read()
    
    # 1. Background to canvas
    if 'android:background' not in content:
        content = content.replace('<androidx.constraintlayout.widget.ConstraintLayout', '<androidx.constraintlayout.widget.ConstraintLayout\n    android:background="@color/cb_canvas"')
        
    # 2. Text colors to ink
    content = content.replace('android:textColor="@color/black"', 'android:textColor="@color/cb_ink"')
    
    # 3. Apply bg_btn_primary to Say button. Use regex to inject background and textColor
    content = re.sub(r'(android:id="@+id/\w+_say"[^>]*?android:layout_height="\d+dp")', r'\1\n        android:background="@drawable/bg_btn_primary"\n        android:textColor="@color/white"', content)
    
    # 4. Apply bg_btn_secondary to Reset button
    content = re.sub(r'(android:id="@+id/\w+_button_reset"[^>]*?android:layout_height="\d+dp")', r'\1\n        android:background="@drawable/bg_btn_secondary"\n        android:textColor="@color/cb_ink"', content)
    
    # Ensure URL texts are also ink color if they don't have a textColor defined yet
    if 'android:text="www.icuk.cz"' in content and 'android:textColor' not in content.split('www.icuk.cz')[0][-100:]:
        content = content.replace('android:text="www.icuk.cz"', 'android:text="www.icuk.cz"\n        android:textColor="@color/cb_blue"')
    if 'android:text="www.ujep.cz"' in content and 'android:textColor' not in content.split('www.ujep.cz')[0][-100:]:
        content = content.replace('android:text="www.ujep.cz"', 'android:text="www.ujep.cz"\n        android:textColor="@color/cb_blue"')

    with open(f, 'w', encoding='utf-8') as file: 
        file.write(content)
    print(f"Updated {f}")
