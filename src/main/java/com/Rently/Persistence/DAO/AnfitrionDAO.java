package com.Rently.Persistence.DAO;

import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Persistence.Entity.Anfitrion;
import com.Rently.Persistence.Mapper.PersonaMapper;
import com.Rently.Persistence.Repository.AnfitrionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AnfitrionDAO {

    private final AnfitrionRepository anfitrionRepository;
    private final PersonaMapper personaMapper;

    public AnfitrionDAO(AnfitrionRepository anfitrionRepository, PersonaMapper personaMapper) {
        this.anfitrionRepository = anfitrionRepository;
        this.personaMapper = personaMapper;
    }


    public AnfitrionDTO crearAnfitrion(AnfitrionDTO dto) {
        Anfitrion anfitrion = personaMapper.dtoToAnfitrion(dto);
        anfitrion.setContrasena(dto.getContrasena());
        Anfitrion saved = anfitrionRepository.save(anfitrion);
        return personaMapper.anfitrionToDTO(saved);
    }

    public Optional<AnfitrionDTO> buscarPorId(Long id) {
        return anfitrionRepository.findById(id)
                .map(personaMapper::anfitrionToDTO);
    }

    public Optional<AnfitrionDTO> buscarPorEmail(String email) {
        return anfitrionRepository.findByEmail(email)
                .map(personaMapper::anfitrionToDTO);
    }

    public List<AnfitrionDTO> buscarPorNombre(String nombre) {
        return personaMapper.anfitrionesToDTO(anfitrionRepository.findByNombreContainingIgnoreCase(nombre));
    }

    public List<AnfitrionDTO> listarTodos() {
        return personaMapper.anfitrionesToDTO(anfitrionRepository.findAll());
    }

    public Optional<AnfitrionDTO> actualizarAnfitrion(Long id, AnfitrionDTO dto) {
        return anfitrionRepository.findById(id).map(anfitrion -> {
            personaMapper.updateAnfitrionFromDTO(anfitrion, dto);
            Anfitrion updated = anfitrionRepository.save(anfitrion);
            return personaMapper.anfitrionToDTO(updated);
        });
    }

    public boolean eliminarAnfitrion(Long id) {
        if (anfitrionRepository.existsById(id)) {
            anfitrionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
