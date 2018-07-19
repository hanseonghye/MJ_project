import os
import json
import detect

dir_name=raw_input("input dir : ")
print (dir_name)
Dir='MYdata/'+dir_name+'/'
for root, dirs, files in os.walk(Dir):
    for fname in files:
        full_name=os.path.join(root, fname)

        RE_json=dict()
        RE_json["label"]=detect.MY_detect_labels(full_name)
        RE_json["landmakr"]=detect.MY_detect_landmarks(full_name)
        RE_json["color"]=detect.MY_detect_properties(full_name)
        (RE_json["top-label"], RE_json["result-web"])=detect.MY_detect_web(full_name)

        save_dir='JSONdata/'+dir_name+'/'
        save_name=fname.split('.')
        save_name=save_name[0]
        save_name=save_dir+save_name+'.json'

        if not os.path.isdir(save_dir):
            os.mkdir(save_dir)



        with open(save_name,'w') as make_file:
            json.dump(RE_json, make_file, indent=2)

    print ('end')
