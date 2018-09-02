import os
import glob
import json
from flask import Flask, render_template, request
from pprint import pprint
import detect
import funcs

app = Flask(__name__)
APP_ROOT = os.path.dirname(os.path.abspath(__file__))
target = os.path.join(APP_ROOT, 'images/')
url_target = os.path.join(APP_ROOT, 'URL/')

target_list = []
fileDirList = os.path.join(APP_ROOT, 'JSONdata2/')
fileDirList = glob.glob(fileDirList + "*.json")

# load json from fileDirList
def load_json():
    global target_list
    target_list=[]
    for fileDir in fileDirList:
        with open(fileDir) as f:
            temp_json = json.load(f)
            temp_dict = dict(fileName=str(os.path.basename(f.name)), jsonDict=temp_json)
            target_list.append(temp_dict)
            temp_dict = {}

@app.route("/")
def index():
    load_json()
    return ;

@app.route('/upload', methods=['POST'])
def upload():
    global target_list

    if not target_list:
        load_json()

    lat = float(request.form['latitude'])
    lon = float(request.form['longitude'])

    file = request.files['uploaded_file']
    des = "/".join([target, "photo.jpg"])
    file.save(des)

    RE_json = dict()
    RE_json["label"] = detect.MY_detect_labels(des)
    RE_json["landmark"] = detect.MY_detect_landmarks(des)
    RE_json["color"] = detect.MY_detect_properties(des)
    (RE_json["top-label"], RE_json["result-web"]) = detect.MY_detect_web(des)

    # with open('jsonfile.json','w') as make_file:
    #     json.dump(RE_json, make_file, indent=2)

    best = funcs.get_score(RE_json, target_list, APP_ROOT, lat, lon)
    return best

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=3003, debug=True)
