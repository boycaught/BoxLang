package ortus.boxlang.runtime.cache.policies;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ortus.boxlang.runtime.cache.ICacheEntry;
import ortus.boxlang.runtime.scopes.Key;

public class MRUTest extends BasePolicyTest {

	@Test
	@DisplayName( "MRU Cache Policy: Comparator Test" )
	void testComparator() {
		// Create 3 cache entries with different return lastAccess in ascending order
		// Put them in a ConcurrentHashMap
		ICacheEntry entry1 = createMockEntry();
		Mockito.when( entry1.lastAccessed() ).thenReturn( Instant.now() );
		ICacheEntry entry2 = createMockEntry();
		Mockito.when( entry2.lastAccessed() ).thenReturn( Instant.now().plusSeconds( 10 ) );
		ICacheEntry entry3 = createMockEntry();
		Mockito.when( entry3.lastAccessed() ).thenReturn( Instant.now().plusSeconds( 20 ) );

		cache.put( Key.of( "entry1" ), entry1 );
		cache.put( Key.of( "entry2" ), entry2 );
		cache.put( Key.of( "entry3" ), entry3 );

		MRU policy = new MRU();

		// Test the ordering according to the lastAccessed
		assertEntries( policy, entry3, entry2, entry1 );
	}

}
