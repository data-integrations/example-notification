# Notification Example using Post Run Action.

<img  alt="Not Available in Cask Market" src="https://cdap-users.herokuapp.com/assets/cm-notavailable.svg"/> [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Join CDAP community](https://cdap-users.herokuapp.com/badge.svg?t=wrangler)](https://cdap-users.herokuapp.com?t=1)


This is a an example repository for building notification using PostAction plugin type.
These type of plugins are executed currently only within batch pipelines. They are
executed at the end of the pipeline execution irrespective of the status of the
pipeline.

Failure in execution of these type of plugins don't fail the pipeline. Use this plugin 
type to send notifications to external system or notify other workflows. 

# Get Started

Clone this repository

```
  git clone https://github.com/hydrator/example-notification.git
```

## Build
```
  mvn clean package
```

# Mailing Lists

CDAP User Group and Development Discussions:

- `cdap-user@googlegroups.com <https://groups.google.com/d/forum/cdap-user>`__

The *cdap-user* mailing list is primarily for users using the product to develop
applications or building plugins for appplications. You can expect questions from 
users, release announcements, and any other discussions that we think will be helpful 
to the users.


# License and Trademarks

Copyright © 2016-2017 Cask Data, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the 
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
either express or implied. See the License for the specific language governing permissions 
and limitations under the License.

Cask is a trademark of Cask Data, Inc. All rights reserved.

Apache, Apache HBase, and HBase are trademarks of The Apache Software Foundation. Used with
permission. No endorsement by The Apache Software Foundation is implied by the use of these marks.
