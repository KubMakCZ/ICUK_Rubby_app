import os

file_path = 'app/src/main/res/raw/main.top'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

fallback_rule = """
u:(e:Dialog/NotUnderstood) ^rand["Tohle zní jako otázka na Google Gemini! Já jsem ale lokální robotka zaměřená hlavně na Inovační centrum a školy v Ústeckém kraji. Zeptejte se mě raději na to." "Na tuhle otázku bohužel neznám odpověď, nejsem připojená na Google. Ale moc ráda vám povím něco o inovačním centru Í cuk, nebo univerzitě Ujep." "To je zajímavé téma, ale moje paměť je plná hlavně informací o Ústeckém kraji a inovacích. Co by vás zajímalo z této oblasti?"]
"""

if 'Dialog/NotUnderstood' not in content:
    content += fallback_rule
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Fallback rule added.")
else:
    print("Fallback rule already exists.")
