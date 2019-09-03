package stream.api;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.Test;

import common.test.tool.annotation.Difficult;
import common.test.tool.annotation.Easy;
import common.test.tool.dataset.ClassicOnlineStore;
import common.test.tool.entity.Customer;
import common.test.tool.entity.Item;
import common.test.tool.util.CollectorImpl;

public class Exercise9Test extends ClassicOnlineStore {
	@Easy
	@Test
	public void simplestStringJoin() {
		List<Customer> customerList = this.mall.getCustomerList();

		/**
		 * Implement a {@link Collector} which can create a String with comma
		 * separated names shown in the assertion. The collector will be used by
		 * serial stream.
		 */
		Supplier<StringBuffer> supplier = () -> {
			return new StringBuffer();
		};
		BiConsumer<StringBuffer, String> accumulator = (sb, s) -> sb.append(s).append(",");
		BinaryOperator<StringBuffer> combiner = (sb, s) -> sb.append(s).append(",");
		Function<StringBuffer, String> finisher = sb -> sb.subSequence(0, sb.lastIndexOf(",")).toString();

		Collector<String, ?, String> toCsv = new CollectorImpl<>(supplier, accumulator, combiner, finisher,
				Collections.emptySet());
		String nameAsCsv = customerList.stream().map(Customer::getName).collect(toCsv);
		assertThat(nameAsCsv, is("Joe,Steven,Patrick,Diana,Chris,Kathy,Alice,Andrew,Martin,Amy"));
	}

	@Difficult
	@Test
	public void mapKeyedByItems() {
		List<Customer> customerList = this.mall.getCustomerList();

		/**
		 * Implement a {@link Collector} which can create a {@link Map} with
		 * keys as item and values as {@link Set} of customers who are wanting
		 * to buy that item. The collector will be used by parallel stream.
		 */
        Supplier<Map<String, Set<String>>> supplier = () -> {
        	return new HashMap<String, Set<String>>();
        };

		BiConsumer<Map<String, Set<String>>, Customer> accumulator = (map, c) -> {

			c.getWantToBuy().stream().forEach(i -> map.computeIfAbsent(i.getName(), x -> new HashSet<>()).add(c.getName()));

		};
		BinaryOperator<Map<String, Set<String>>> combiner = (map1, map2) -> {
			map2.forEach((key, value) -> map1.merge(key, value, (v1, v2) -> {
				v1.addAll(v2);
				return v1;
			}));
			return map1;
		};
		
		

        Function<Map<String, Set<String>>, Map<String, Set<String>>> finisher = null;

		Collector<Customer, ?, Map<String, Set<String>>> toItemAsKey = new CollectorImpl<>(supplier, accumulator,
				combiner, finisher,
				EnumSet.of(Collector.Characteristics.CONCURRENT, Collector.Characteristics.IDENTITY_FINISH));
		Map<String, Set<String>> itemMap = customerList.stream().parallel().collect(toItemAsKey);
		assertThat(itemMap.get("plane"), containsInAnyOrder("Chris"));
		assertThat(itemMap.get("onion"), containsInAnyOrder("Patrick", "Amy"));
		assertThat(itemMap.get("ice cream"), containsInAnyOrder("Patrick", "Steven"));
		assertThat(itemMap.get("earphone"), containsInAnyOrder("Steven"));
		assertThat(itemMap.get("plate"), containsInAnyOrder("Joe", "Martin"));
		assertThat(itemMap.get("fork"), containsInAnyOrder("Joe", "Martin"));
		assertThat(itemMap.get("cable"), containsInAnyOrder("Diana", "Steven"));
		assertThat(itemMap.get("desk"), containsInAnyOrder("Alice"));
	}

	@Difficult
	@Test
	public void bitList2BitString() {
		String bitList = "22-24,9,42-44,11,4,46,14-17,5,2,38-40,33,50,48";

		/**
		 * Create a {@link String} of "n"th bit ON. for example "3" will be
		 * "001" "1,3,5" will be "10101" "1-3" will be "111" "7,1-3,5" will be
		 * "1110101"
		 */
		Collector<String, ?, String> toBitString = null;

		String bitString = Arrays.stream(bitList.split(",")).collect(toBitString);
		assertThat(bitString, is("01011000101001111000011100000000100001110111010101")

		);
	}

}
