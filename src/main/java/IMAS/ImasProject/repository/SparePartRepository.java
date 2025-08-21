package IMAS.ImasProject.repository;


import IMAS.ImasProject.model.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for SparePart entity.
 * Provides methods to interact with the database.
 */
@Repository
public interface SparePartRepository extends JpaRepository<SparePart, Long> {

    /**
     * Finds spare parts by name (case-insensitive, partial match).
     * @param name The name to search for
     * @return A list of matching spare parts
     */
    List<SparePart> findByNameContainingIgnoreCase(String name);

    /**
     * Finds spare parts by supplier.
     * @param supplier The supplier name
     * @return A list of spare parts from the specified supplier
     */
    List<SparePart> findBySupplier(String supplier);

    /**
     * Finds spare parts by location.
     * @param location The storage location
     * @return A list of spare parts at the specified location
     */
    List<SparePart> findByLocation(String location);

    /**
     * Finds all spare parts that are below their minimum stock level.
     * @return A list of spare parts with low stock
     */
    @Query("SELECT s FROM SparePart s WHERE s.quantity <= s.minimumStockLevel")
    List<SparePart> findLowStockParts();

    /**
     * Finds spare parts with quantity equal to zero.
     * @return A list of out-of-stock spare parts
     */
    List<SparePart> findByQuantity(Integer quantity);

    /**
     * Finds a spare part by its part number.
     * @param partNumber The part number to search for
     * @return The spare part if found
     */
    SparePart findByPartNumber(String partNumber);

    /**
     * Finds spare parts by category.
     * @param category The category to search for
     * @return A list of spare parts in the specified category
     */
    List<SparePart> findByCategory(String category);
}