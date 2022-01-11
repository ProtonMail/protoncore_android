import os
import sys
import xml.etree.ElementTree as ET
from datetime import datetime

INPUT_XML_FILE = sys.argv[1]
OUTPUT_XML_FILE = sys.argv[2]
SCREENSHOT_ELEMENT = '<system-out>[[{name}|{path}]]</system-out>\n'
SCREENSHOT_LOCATION = './screenshots/screenshots/'
CLASSNAME_PREFIX = 'me.proton.core.test.android.uitests.tests.medium.'

results_xml = ET.parse(INPUT_XML_FILE)

for test_case in results_xml.findall('testcase'):

    # Replace classname with short value for better readability
    short_class_name = test_case.get('classname').replace(CLASSNAME_PREFIX, '')
    test_case.set('classname', short_class_name)

    # append screenshot to corresponding test case
    # https://docs.gitlab.com/ee/ci/unit_test_reports.html#viewing-junit-screenshots-on-gitlab
    test_name = test_case.get('name')
    try:
      for filename in os.listdir(SCREENSHOT_LOCATION):
          if test_name in filename:
              screenshot_path = SCREENSHOT_LOCATION[2:] + filename
              test_case.append(ET.XML(SCREENSHOT_ELEMENT.format(name="ATTACHMENT", path=screenshot_path)))
    except(OSError):
      # screenshot directory doesn't exist. do nothing
      pass

results_xml.write(OUTPUT_XML_FILE)
