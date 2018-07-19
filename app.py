import os
import json
import pymysql
from flask import Flask, render_template, request

import detect

conn=pymysql.connect(
        host='localhost',
        user='root',
        password='alde2871',
        db='MJ',
        charset='utf8'
)

curs=conn.cursor()

app=Flask(__name__)
APP_ROOT=os.path.dirname(os.path.abspath(__file__))

@app.route("/")
def index():
    return render_template("home.html")

@app.route("/upload", methods=['POST'])
def upload():

    target=os.path.join(APP_ROOT, 'images/')
    if not os.path.isdir(target):
        os.mkdir(target)

    if 'file' not in request.files:
        return render_template("home.html")

    file=request.files['file']
    filename=file.filename
    des="/".join([target,filename])
    file.save(des)

    RE_json=dict()
    RE_json["label"]=detect.MY_detect_labels(des)
    RE_json["landmark"]=detect.MY_detect_landmarks(des)
    RE_json["color"]=detect.MY_detect_properties(des)
    (RE_json["top-label"], RE_json["result-web"])=detect.MY_detect_web(des)
    # RE_json["result-geo"]=detect.MY_web_entities_include_geo_results(des)

    # with open('jsonfile.json','w') as make_file:
    #     json.dump(RE_json, make_file, indent=2)

    topL=RE_json['top-label']
    if not topL:
        real_topL="nnnn"
    else :
        real_topL=(topL[0]["top label"]).lower()

    reWeb=RE_json['result-web']
    if not reWeb:
        real_reWeb="nnnn"
    else :
        real_reWeb=(reWeb[0]["description"]).lower()
    
    reLM=RE_json["landmark"]
    if not reLM:
        real_reLM="nnnn"
    else :
        real_reLM=(reLM[0]["description"]).lower()


    mapping_re=""
    
    sql="SELECT * FROM MappingInfo WHERE p1='{}'".format(real_reWeb)
    curs.execute(sql)
    find_p1=curs.fetchall()

    if not  find_p1:
            sql="SELECT * FROM MappingInfo WHERE p1='{}'".format(real_topL)
            curs.execute(sql)
            find_p1=curs.fetchall()

            if not find_p1:
                    sql="SELECT * FROM MappingInfo WHERE p1='{}'".format(real_reWeb)
                    curs.execute(sql)
                    find_p1=curs.fetchall()

                    if not find_p1:
                        mapping_re="not matching"
                    else :
                        mapping_re=find_p1[0][2]
            else :
                mapping_re=find_p1[0][2]
    else :
        mapping_re=find_p1[0][2]



    return render_template("pass.html",reMapping=mapping_re ,reLB=real_topL, reLM=real_reLM, reWeb=real_reWeb);

if __name__ == "__main__":
    app.run(host='0.0.0.0',port=3000,debug=True)
