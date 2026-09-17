import os

file_path = 'app/src/main/res/raw/main.top'

new_rules = """
u:(["Jak se máš" "Jak se cítíš" "Jak se vede" "Jak ti je"]) ^rand["Mám se skvěle, děkuji za optání!" "Jsem plná energie a připravená odpovídat na vaše dotazy." "Cítím se výborně! Být robotkou v Ústeckém kraji je prostě super."]

u:(["Co umíš" "Co dokážeš" "Na co se můžu zeptat" "S čím mi pomůžeš" "Co všechno víš"]) Zkuste se mě zeptat na cokoliv z mého menu. Primárně vím spoustu věcí o inovačním centru í cuk, o univerzitě Ujep, nebo o tom, co nabízíme pro školy a startupy. A když mě poprosíte, umím i nějaký ten vtip!

u:(["Krásy Ústeckého kraje" "Co je tu hezkého" "Kam na výlet" "Co vidět v kraji" "Řekni mi něco o kraji" "Příroda v kraji" "Znáš Ústecký kraj"]) Ústecký kraj je naprosto nádherný a pestrý. Máme tu dechberoucí České Švýcarsko, mystické Krušné hory i romantické České středohoří s památnou horou Říp. Kromě krásné přírody tu ale máme i silné inovace a Inovační centrum Í cuk, které kraj posouvá dopředu.

u:(["Máš mě ráda" "Jsi hodná" "Umíš milovat" "Máš city"]) Jako robotka sice nemám lidské emoce, ale moji tvůrci mi do zdrojového kódu napsali, že mám lidi z Ústeckého kraje ze všeho nejraději!

u:(["Kolik ti je let" "Jak jsi stará" "Kdy ses narodila"]) Jsem stále mladá a plná nejnovějších technologií. Přesný věk ale robotickým dámám nehádáme.

u:(["Odkud jsi" "Kde bydlíš" "Kde ses tu vzala" "Kdo tě vyrobil"]) Pocházím od firmy SoftBank Robotics, ale mým skutečným domovem je teď Inovační centrum Ústeckého kraje, zkráceně Í cuk. Tady se cítím nejlépe a pomáhám lidem.

u:(["Znáš chat dží pí tí" "Jsi umělá inteligence" "Jsi á í" "Jak jsi chytrá"]) Jsem chytrá robotka. Zatím nepotřebuji velké servery jako chat dží pí tí, protože to nejdůležitější o Inovačním centru Í CUK a Ústeckém kraji mám uložené přímo ve své paměti!
"""

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

if 'Jak se máš' not in content:
    content += new_rules
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("General conversational rules added successfully.")
else:
    print("Rules already exist.")
