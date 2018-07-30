import os
import json
import detect


Dir='crw_image/'

for root, dirs, files in os.walk(Dir):
    for fname in files:
        full_name=os.path.join(root, fname)

        save_dir='JSONdata2/'
        save_name=fname.split('.')
        save_name=save_name[0]
        save_name=save_dir+save_name+'.json'

        if os.path.isfile(save_name) == False :
            RE_json=dict()
            RE_json["label"]=detect.MY_detect_labels(full_name)
            RE_json["landmark"]=detect.MY_detect_landmarks(full_name)
            RE_json["color"]=detect.MY_detect_properties(full_name)
            (RE_json["top-label"], RE_json["result-web"])=detect.MY_detect_web(full_name)


            print(save_name)

            with open(save_name,'w') as make_file:
                json.dump(RE_json, make_file, indent=2)

    print ('end')
