import os
import json
import detect

Dir='url/'
for root, dirs, files in os.walk(Dir):
    for fname in files:
        full_name=os.path.join(root, fname)
        f=open(full_name,'r')
        url=f.readline()
        f.close()

        RE_json=dict()
        RE_json["label"]=detect.MY_detect_labels_URL(url)
        RE_json["landmark"]=detect.MY_detect_landmarks_URL(url)
        RE_json["color"]=detect.MY_detect_properties_URL(url)
        (RE_json["top-label"], RE_json["result-web"])=detect.MY_detect_web_URL(url)

        save_dir='json/'
        save_name=fname.split('.')
        save_name=save_name[0]
        save_name=save_dir+save_name+'.json'

        print(save_name)

        with open(save_name,'w') as make_file:
            json.dump(RE_json, make_file, indent=2)

    print ('end')
