package com.example.demo.services;
import com.example.demo.dto.UsuarioDTO;import com.example.demo.models.Persona;import com.example.demo.repository.PersonaRepository;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.util.List;
@Service
public class UsuarioService{
    private final PersonaRepository repository;private final PasswordEncoder encoder;public UsuarioService(PersonaRepository repository,PasswordEncoder encoder){this.repository=repository;this.encoder=encoder;}
    @Transactional(readOnly=true)public List<UsuarioDTO> listar(){return repository.findAll().stream().map(this::dto).toList();}
    @Transactional public UsuarioDTO guardar(Long id,UsuarioDTO value,String password){Persona p=id==null?new Persona():repository.findById(id).orElseThrow(()->new IllegalArgumentException("Usuario inexistente"));
        if(value.nombre()==null||value.nombre().isBlank()||value.email()==null||value.email().isBlank())throw new IllegalArgumentException("Nombre y email son obligatorios");
        repository.findByEmail(value.email().trim().toLowerCase()).filter(other->other.getId()!=p.getId()).ifPresent(other->{throw new IllegalArgumentException("El email ya está registrado");});
        if(id==null&&(password==null||password.length()<6))throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        p.setNombre(value.nombre().trim());p.setApellido(value.apellido()==null?"":value.apellido().trim());p.setDni(value.dni());p.setEmail(value.email().trim().toLowerCase());p.setEdad(18);p.setRol(Persona.Rol.valueOf(value.rol()));p.setActivo(value.activo());if(password!=null&&!password.isBlank())p.setContraseña(encoder.encode(password));return dto(repository.save(p));}
    private UsuarioDTO dto(Persona p){return new UsuarioDTO(p.getId(),p.getNombre(),p.getApellido(),p.getDni(),p.getEmail(),p.getRol().name(),p.isActivo());}
}
