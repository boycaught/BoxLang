// Static reference to System (java proxy?)

variables['system'] = createObject('java','java.lang.System');
// Static reference to String
variables.greeting = createObject('java','java.lang.String')
// call constructor to create instance
.init( 'Hello' );

// Conditional, requires operation support
if( variables.greeting == 'Hello' ) {
// De-referencing "out" and "println" and calling Java method via invoke dynamic
//variables.system.out.println(
  // Multi-line statement, expression requires concat operator and possible casting
  // Unscoped lookup requires scope search
//  greeting & " world"
//)

} else {
variables['system'] = createObject('java','java.lang.System');
}
switch(variables['a']) {
case "9": {
variables['a'] = "0";
break;
}
case "1": {
variables['a'] = "1";
break;
}
default: {
variables['a'] = "default";
break;
}
}


