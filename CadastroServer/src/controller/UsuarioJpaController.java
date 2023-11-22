
package controller;

import controller.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import model.Movimentacao;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import model.Usuario;

/**
 *
 * @author AlarconAbap
 */
public class UsuarioJpaController implements Serializable {

    public UsuarioJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Usuario usuario) {
        if (usuario.getMovimentacaoCollection() == null) {
            usuario.setMovimentacaoCollection(new ArrayList<Movimentacao>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Movimentacao> attachedMovimentacaoCollection = new ArrayList<Movimentacao>();
            for (Movimentacao movimentacaoCollectionMovimentacaoToAttach : usuario.getMovimentacaoCollection()) {
                movimentacaoCollectionMovimentacaoToAttach = em.getReference(movimentacaoCollectionMovimentacaoToAttach.getClass(), movimentacaoCollectionMovimentacaoToAttach.getIdMovimento());
                attachedMovimentacaoCollection.add(movimentacaoCollectionMovimentacaoToAttach);
            }
            usuario.setMovimentacaoCollection(attachedMovimentacaoCollection);
            em.persist(usuario);
            for (Movimentacao movimentacaoCollectionMovimentacao : usuario.getMovimentacaoCollection()) {
                Usuario oldIdUsuarioOfMovimentacaoCollectionMovimentacao = movimentacaoCollectionMovimentacao.getIdUsuario();
                movimentacaoCollectionMovimentacao.setIdUsuario(usuario);
                movimentacaoCollectionMovimentacao = em.merge(movimentacaoCollectionMovimentacao);
                if (oldIdUsuarioOfMovimentacaoCollectionMovimentacao != null) {
                    oldIdUsuarioOfMovimentacaoCollectionMovimentacao.getMovimentacaoCollection().remove(movimentacaoCollectionMovimentacao);
                    oldIdUsuarioOfMovimentacaoCollectionMovimentacao = em.merge(oldIdUsuarioOfMovimentacaoCollectionMovimentacao);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Usuario usuario) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuario persistentUsuario = em.find(Usuario.class, usuario.getIdUsuario());
            Collection<Movimentacao> movimentacaoCollectionOld = persistentUsuario.getMovimentacaoCollection();
            Collection<Movimentacao> movimentacaoCollectionNew = usuario.getMovimentacaoCollection();
            Collection<Movimentacao> attachedMovimentacaoCollectionNew = new ArrayList<Movimentacao>();
            for (Movimentacao movimentacaoCollectionNewMovimentacaoToAttach : movimentacaoCollectionNew) {
                movimentacaoCollectionNewMovimentacaoToAttach = em.getReference(movimentacaoCollectionNewMovimentacaoToAttach.getClass(), movimentacaoCollectionNewMovimentacaoToAttach.getIdMovimento());
                attachedMovimentacaoCollectionNew.add(movimentacaoCollectionNewMovimentacaoToAttach);
            }
            movimentacaoCollectionNew = attachedMovimentacaoCollectionNew;
            usuario.setMovimentacaoCollection(movimentacaoCollectionNew);
            usuario = em.merge(usuario);
            for (Movimentacao movimentacaoCollectionOldMovimentacao : movimentacaoCollectionOld) {
                if (!movimentacaoCollectionNew.contains(movimentacaoCollectionOldMovimentacao)) {
                    movimentacaoCollectionOldMovimentacao.setIdUsuario(null);
                    movimentacaoCollectionOldMovimentacao = em.merge(movimentacaoCollectionOldMovimentacao);
                }
            }
            for (Movimentacao movimentacaoCollectionNewMovimentacao : movimentacaoCollectionNew) {
                if (!movimentacaoCollectionOld.contains(movimentacaoCollectionNewMovimentacao)) {
                    Usuario oldIdUsuarioOfMovimentacaoCollectionNewMovimentacao = movimentacaoCollectionNewMovimentacao.getIdUsuario();
                    movimentacaoCollectionNewMovimentacao.setIdUsuario(usuario);
                    movimentacaoCollectionNewMovimentacao = em.merge(movimentacaoCollectionNewMovimentacao);
                    if (oldIdUsuarioOfMovimentacaoCollectionNewMovimentacao != null && !oldIdUsuarioOfMovimentacaoCollectionNewMovimentacao.equals(usuario)) {
                        oldIdUsuarioOfMovimentacaoCollectionNewMovimentacao.getMovimentacaoCollection().remove(movimentacaoCollectionNewMovimentacao);
                        oldIdUsuarioOfMovimentacaoCollectionNewMovimentacao = em.merge(oldIdUsuarioOfMovimentacaoCollectionNewMovimentacao);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = usuario.getIdUsuario();
                if (findUsuario(id) == null) {
                    throw new NonexistentEntityException("O Produto de id " + id + " não existe..");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuario usuario;
            try {
                usuario = em.getReference(Usuario.class, id);
                usuario.getIdUsuario();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("O Produto de id " + id + " não existe..", enfe);
            }
            Collection<Movimentacao> movimentacaoCollection = usuario.getMovimentacaoCollection();
            for (Movimentacao movimentacaoCollectionMovimentacao : movimentacaoCollection) {
                movimentacaoCollectionMovimentacao.setIdUsuario(null);
                movimentacaoCollectionMovimentacao = em.merge(movimentacaoCollectionMovimentacao);
            }
            em.remove(usuario);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Usuario> findUsuarioEntities() {
        return findUsuarioEntities(true, -1, -1);
    }

    public List<Usuario> findUsuarioEntities(int maxResults, int firstResult) {
        return findUsuarioEntities(false, maxResults, firstResult);
    }

    private List<Usuario> findUsuarioEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Usuario.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Usuario findUsuario(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }

    public int getUsuarioCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Usuario> rt = cq.from(Usuario.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public Usuario findUsuario(String login, String senha) {
    EntityManager em = getEntityManager();
    try {
        Query query = em.createQuery("SELECT u FROM Usuario u WHERE u.login = :login AND u.senha = :senha");
        query.setParameter("login", login);
        query.setParameter("senha", senha);

        return (Usuario) query.getSingleResult();
    } catch (NoResultException e) {
        return null; 
    } finally {
        em.close();
    }
}
    
}
