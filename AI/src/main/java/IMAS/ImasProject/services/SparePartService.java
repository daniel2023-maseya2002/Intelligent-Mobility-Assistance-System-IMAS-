package IMAS.ImasProject.services;


import IMAS.ImasProject.model.SparePart;
import IMAS.ImasProject.repository.SparePartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SparePartService {

    private final SparePartRepository sparePartRepository;

    @Autowired
    public SparePartService(SparePartRepository sparePartRepository) {
        this.sparePartRepository = sparePartRepository;
    }

    public List<SparePart> getAllSpareParts() {
        return sparePartRepository.findAll();
    }

    public Optional<SparePart> getSparePartById(Long id) {
        return sparePartRepository.findById(id);
    }

    public SparePart getSparePartByPartNumber(String partNumber) {
        return sparePartRepository.findByPartNumber(partNumber);
    }

    public List<SparePart> getPartsByCategory(String category) {
        return sparePartRepository.findByCategory(category);
    }

    @Transactional
    public SparePart saveSparePart(SparePart sparePart) {
        if (sparePart.getPartId() == null) { // New part
            SparePart existing = sparePartRepository.findByPartNumber(sparePart.getPartNumber());
            if (existing != null) {
                throw new IllegalArgumentException("Part number already exists");
            }
        } else { // Updating existing part
            SparePart existing = sparePartRepository.findByPartNumber(sparePart.getPartNumber());
            if (existing != null && !existing.getPartId().equals(sparePart.getPartId())) {
                throw new IllegalArgumentException("Part number already exists for another part");
            }
        }
        return sparePartRepository.save(sparePart);
    }

    @Transactional
    public void deleteSparePart(Long id) {
        sparePartRepository.deleteById(id);
    }

    public List<SparePart> searchByName(String name) {
        return sparePartRepository.findByNameContainingIgnoreCase(name);
    }

    public List<SparePart> getPartsBySupplier(String supplier) {
        return sparePartRepository.findBySupplier(supplier);
    }

    public List<SparePart> getPartsByLocation(String location) {
        return sparePartRepository.findByLocation(location);
    }

    public List<SparePart> getLowStockParts() {
        return sparePartRepository.findLowStockParts();
    }

    public List<SparePart> getOutOfStockParts() {
        return sparePartRepository.findByQuantity(0);
    }

    @Transactional
    public boolean decreaseStock(Long id, int amount) {
        Optional<SparePart> optionalPart = sparePartRepository.findById(id);
        if (optionalPart.isPresent()) {
            SparePart part = optionalPart.get();
            boolean result = part.decreaseStock(amount);
            if (result) {
                sparePartRepository.save(part);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean increaseStock(Long id, int amount) {
        Optional<SparePart> optionalPart = sparePartRepository.findById(id);
        if (optionalPart.isPresent()) {
            SparePart part = optionalPart.get();
            boolean result = part.increaseStock(amount);
            if (result) {
                sparePartRepository.save(part);
                return true;
            }
        }
        return false;
    }

    public String orderPart(Long id, int amount) {
        Optional<SparePart> optionalPart = sparePartRepository.findById(id);
        if (optionalPart.isPresent()) {
            SparePart part = optionalPart.get();
            return part.orderPart(amount);
        }
        return "Part not found";
    }

    public Set<String> getAllCategories() {
        return sparePartRepository.findAll().stream()
                .map(SparePart::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .collect(Collectors.toSet());
    }
}