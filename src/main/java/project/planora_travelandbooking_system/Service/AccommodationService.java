package project.planora_travelandbooking_system.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.planora_travelandbooking_system.Model.Accommodation;
import project.planora_travelandbooking_system.Repository.AccommodationRepository;
import java.util.List;
import java.util.Optional;

@Service
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;

    @Autowired
    public AccommodationService(AccommodationRepository accommodationRepository) {
        this.accommodationRepository = accommodationRepository;
    }

    public Accommodation saveHotel(Accommodation accommodation) {
        return accommodationRepository.save(accommodation);
    }

    public List<Accommodation> getAllHotels() {
        return accommodationRepository.findAll();
    }

    public Optional<Accommodation> getHotelById(Long id) {
        return accommodationRepository.findById(id);
    }

    public void deleteHotel(Long id) {
        accommodationRepository.deleteById(id);
    }

}