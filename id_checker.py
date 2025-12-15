import os
import sys
import xml.etree.ElementTree as ET

def get_line_numbers(xml_file, id_name):
    """
    Scans the file text to find line numbers matching the ID declaration.
    """
    lines = []
    target_double = f'android:id="@+id/{id_name}"'
    target_single = f"android:id='@+id/{id_name}'"
    
    try:
        with open(xml_file, 'r', encoding='utf-8') as f:
            for line_num, line in enumerate(f, 1):
                if target_double in line or target_single in line:
                    lines.append(str(line_num))
    except Exception:
        pass
    return lines

def check_duplicate_ids(xml_file):
    # this is the XML namespace used in Android XML files
    ANDROID_NS = 'http://schemas.android.com/apk/res/android'
    has_duplicates = False
    try:
        # parse the XML file
        tree = ET.parse(xml_file)
        # get the root element
        root = tree.getroot()
        #  create a dictionary to store the IDs and their corresponding elements
        id_map = {}

        # this is just for loging, it will print this filename not the full path
        file_name_only = os.path.basename(xml_file)

        # iterate through all the elements in the XML file
        for elem in root.iter():
            # only check the android:id attributes
            android_id = elem.get(f'{{{ANDROID_NS}}}id')
            # checking if the android:id attribute exists and is not empty or none
            if android_id and android_id.startswith('@+id/'):
                # Extract the ID name from the format for example @+id/button1
                id_name = android_id.split('/')[-1]
                if id_name in id_map:
                    # Get line numbers for better reporting
                    lines = get_line_numbers(xml_file, id_name)
                    line_info = ", ".join(lines) if lines else "unknown"

                    print(f"‼️ ERROR: Duplicate ID declaration found in '{file_name_only}': '{id_name}'")
                    print(f"  Found on lines: {line_info}")
                    print(f"  First occurrence: {id_map[id_name].tag}")
                    print(f"  Second occurrence: {elem.tag}")
                    print()
                    has_duplicates = True
                else:
                    id_map[id_name] = elem

        if not has_duplicates:
             print(f"✅ ID check complete for '{file_name_only}'. No duplicates.")
             return True # Success
        else:
             return False # Failed

    except ET.ParseError:
        print(f"❌ Failed to parse XML file: '{xml_file}'\n")
        return False # Failed

def main():
    # path to the android layout files
    layout_dir = os.path.join('app', 'src', 'main', 'res', 'layout')
    #check if the file exists
    if not os.path.exists(layout_dir):
        print(f"❌ Directory not found: {layout_dir}")
        sys.exit(1) # Exit with error

    print(f"Checking for duplicate IDs in: {layout_dir}\n")

    overall_success = True

    # get all the xml files in the directory
    for filename in os.listdir(layout_dir):
        if filename.endswith('.xml'):
            full_path = os.path.join(layout_dir, filename)
            # checking for duplicates in each xml file using the function
            if not check_duplicate_ids(full_path):
                overall_success = False

    if not overall_success:
        print("\n❌ Build Failed: Duplicate IDs detected.")
        sys.exit(1) # Non-zero exit code stops the workflow
    else:
        print("\n✅ All checks passed.")
        sys.exit(0) # Zero exit code means success

if __name__ == "__main__":
    main()
