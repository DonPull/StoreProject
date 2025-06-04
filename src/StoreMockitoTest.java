import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreMockitoTest {

    @Mock
    private Product mockedProduct;

    @Mock
    private Cashier mockedCashier;

    private Store store;

    // store config constants matching the ones in StoreTest setup for consistency
    private static final double FOOD_MARKUP = 10.0;
    private static final double NON_FOOD_MARKUP = 20.0;
    private static final long DISCOUNT_THRESHOLD_DAYS = 3;
    private static final double DISCOUNT_PERCENT = 5.0;


    @BeforeEach
    void setUp() {
        store = new Store("Mockito Simple Store", FOOD_MARKUP, NON_FOOD_MARKUP, DISCOUNT_THRESHOLD_DAYS, DISCOUNT_PERCENT);
        
        when(mockedCashier.getId()).thenReturn("C_MOCK");
        when(mockedCashier.getName()).thenReturn("Mock Cashier");
        store.addCashier(mockedCashier); 
    }
    
    @Test
    void testSell_simpleSuccessfulSale_withMockedProduct() throws Exception {
        when(mockedProduct.getId()).thenReturn("P_MOCK");
        when(mockedProduct.getName()).thenReturn("Mocked Drink");
        when(mockedProduct.getQuantity()).thenReturn(10); 
        when(mockedProduct.getCategory()).thenReturn(Category.FOOD);
        when(mockedProduct.getExpiryDate()).thenReturn(LocalDate.now().plusDays(30));
        when(mockedProduct.getCostPrice()).thenReturn(1.0);

        when(mockedProduct.getSellingPrice(FOOD_MARKUP, DISCOUNT_THRESHOLD_DAYS, DISCOUNT_PERCENT)).thenReturn(1.10);

        store.loadProduct(mockedProduct); 

        Map<String, Integer> order = new HashMap<>();
        order.put("P_MOCK", 2);

        Receipt receipt = store.sell(mockedCashier.getId(), order);

        assertNotNull(receipt, "receipt should not be null after a sale.");
        assertEquals(1, receipt.getLineItems().size(), "receipt should have one line item.");
        assertEquals("Mocked Drink", receipt.getLineItems().get(0).getProduct().getName(), "product name in receipt should be as mocked.");
        assertEquals(2, receipt.getLineItems().get(0).getQuantity(), "quantity in receipt should be 2.");
        assertEquals(1.10, receipt.getLineItems().get(0).getPrice(), 0.001, "price in receipt should be the mocked selling price.");
        assertEquals(2 * 1.10, receipt.getTotal(), 0.001, "total of receipt should be correct.");

        verify(mockedProduct, times(1)).reduceQuantity(2);
        verify(mockedProduct, times(1)).getSellingPrice(FOOD_MARKUP, DISCOUNT_THRESHOLD_DAYS, DISCOUNT_PERCENT);
        
        // cleanup
        new File("receipt_" + receipt.getNumber() + ".txt").delete();
        new File("receipt_" + receipt.getNumber() + ".ser").delete();
    }
}