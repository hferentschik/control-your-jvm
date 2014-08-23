# Wildfly Kitchensink

[wildfly-kitchensink.war](#wildfly-kitchensink.war) is based on a slightly modified version of
the Wildfly Kitchensink demo which can be be found [here](https://github.com/wildfly/quickstart/tree/master/kitchensink).

The code is slightly modified in order to trigger the bug described in
[HV-838](https://hibernate.atlassian.net/browse/HV-838). To trigger the bug the valiated class needs
to extend another class (not java.lang.Object!) or implement an interface. In the demo war a _IMember_
interface is introduced for this purpose.
