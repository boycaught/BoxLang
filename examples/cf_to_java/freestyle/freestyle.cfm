<cfscript>
	variables['system'] = createObject('java','java.lang.System');
	a = 0;
	b = false;
	c = 0;
	// While loop
	while(a < 10) {
		c = 0;
		// Switch statement
		switch (a) {
			case 0: {
				variables.system.out.println("#a# = zero!!!");
				break;
			}
			case 1: {
				variables.system.out.println("#a# = one");
				break;
			}
			default: {
				variables.system.out.println("non zero or one");
				break;
			}
		}
		// Try catch
		try {
			a/=a % 2;
			// throw new java:java.lang.RuntimeException( "My Message" );
		} catch (any e) {
			variables.system.out.println("exception " & a);
		} finally {
			variables.system.out.println("finally " & a);
			b = true;
			if(!b == false) {
				variables.system.out.println("b = #b# ");
			}
		}

		for(c = 0; c < 2; c++){
			variables.system.out.println(c);
		}
		// if statement
		if(!a % 2 == 0 && a > 5 ) {
			variables.system.out.println("even and a=#variables.a#, b = #b#");
		}
		a++;
	}
	assert(a == 9)


</cfscript>