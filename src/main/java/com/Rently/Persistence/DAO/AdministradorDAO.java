package com.Rently.Persistence.DAO;

import com.Rently.Business.DTO.AdministradorDTO;
import com.Rently.Persistence.Entity.Administrador;
import com.Rently.Persistence.Mapper.PersonaMapper;
import com.Rently.Persistence.Repository.AdministradorRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AdministradorDAO {

    private final AdministradorRepository administradorRepository;
    private final PersonaMapper personaMapper;

    public AdministradorDAO(AdministradorRepository administradorRepository, PersonaMapper personaMapper) {
        this.administradorRepository = administradorRepository;
        this.personaMapper = personaMapper;
    }

    public AdministradorDTO crearAdministrador(AdministradorDTO dto) {
        return crearAdministrador(dto, dto.getContrasena());
    }

    public AdministradorDTO crearAdministrador(AdministradorDTO dto, String contrasenaCodificada) {
        Administrador admin = personaMapper.dtoToAdmin(dto);
        admin.setContrasena(contrasenaCodificada);
        Administrador saved = administradorRepository.save(admin);
        return personaMapper.adminToDTO(saved);
    }

    public Optional<AdministradorDTO> buscarPorId(Long id) {
        return administradorRepository.findById(id)
                .map(personaMapper::adminToDTO);
    }

    public Optional<AdministradorDTO> buscarPorEmail(String email) {
        return administradorRepository.findByEmail(email)
                .map(personaMapper::adminToDTO);
    }

    public List<AdministradorDTO> listarTodos() {
        return personaMapper.adminsToDTO(administradorRepository.findAll());
    }

    public Optional<AdministradorDTO> actualizarAdministrador(Long id, AdministradorDTO dto) {
        return administradorRepository.findById(id).map(admin -> {
            personaMapper.updateAdminFromDTO(admin, dto);
            Administrador updated = administradorRepository.save(admin);
            return personaMapper.adminToDTO(updated);
        });
    }

    public boolean eliminarAdministrador(Long id) {
        if (administradorRepository.existsById(id)) {
            administradorRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
