
package controller;

import controller.exceptions.NonexistentEntityException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import model.Movimentacao;
import model.Pessoa;
import model.Produto;
import model.Usuario;

/**
 *
 * @author AlarconAbap
 */
public class MovimentacaoJpaController implements Serializable {

    public MovimentacaoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Movimentacao movimentacao) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pessoa idPessoa = movimentacao.getIdPessoa();
            if (idPessoa != null) {
                idPessoa = em.getReference(idPessoa.getClass(), idPessoa.getIdPessoa());
                movimentacao.setIdPessoa(idPessoa);
            }
            Produto idProduto = movimentacao.getIdProduto();
            if (idProduto != null) {
                idProduto = em.getReference(idProduto.getClass(), idProduto.getIdProduto());
                movimentacao.setIdProduto(idProduto);
            }
            Usuario idUsuario = movimentacao.getIdUsuario();
            if (idUsuario != null) {
                idUsuario = em.getReference(idUsuario.getClass(), idUsuario.getIdUsuario());
                movimentacao.setIdUsuario(idUsuario);
            }
            em.persist(movimentacao);
            if (idPessoa != null) {
                idPessoa.getMovimentacaoCollection().add(movimentacao);
                idPessoa = em.merge(idPessoa);
            }
            if (idProduto != null) {
                idProduto.getMovimentacaoCollection().add(movimentacao);
                idProduto = em.merge(idProduto);
            }
            if (idUsuario != null) {
                idUsuario.getMovimentacaoCollection().add(movimentacao);
                idUsuario = em.merge(idUsuario);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Movimentacao movimentacao) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Movimentacao persistentMovimentacao = em.find(Movimentacao.class, movimentacao.getIdMovimento());
            Pessoa idPessoaOld = persistentMovimentacao.getIdPessoa();
            Pessoa idPessoaNew = movimentacao.getIdPessoa();
            Produto idProdutoOld = persistentMovimentacao.getIdProduto();
            Produto idProdutoNew = movimentacao.getIdProduto();
            Usuario idUsuarioOld = persistentMovimentacao.getIdUsuario();
            Usuario idUsuarioNew = movimentacao.getIdUsuario();
            if (idPessoaNew != null) {
                idPessoaNew = em.getReference(idPessoaNew.getClass(), idPessoaNew.getIdPessoa());
                movimentacao.setIdPessoa(idPessoaNew);
            }
            if (idProdutoNew != null) {
                idProdutoNew = em.getReference(idProdutoNew.getClass(), idProdutoNew.getIdProduto());
                movimentacao.setIdProduto(idProdutoNew);
            }
            if (idUsuarioNew != null) {
                idUsuarioNew = em.getReference(idUsuarioNew.getClass(), idUsuarioNew.getIdUsuario());
                movimentacao.setIdUsuario(idUsuarioNew);
            }
            movimentacao = em.merge(movimentacao);
            if (idPessoaOld != null && !idPessoaOld.equals(idPessoaNew)) {
                idPessoaOld.getMovimentacaoCollection().remove(movimentacao);
                idPessoaOld = em.merge(idPessoaOld);
            }
            if (idPessoaNew != null && !idPessoaNew.equals(idPessoaOld)) {
                idPessoaNew.getMovimentacaoCollection().add(movimentacao);
                idPessoaNew = em.merge(idPessoaNew);
            }
            if (idProdutoOld != null && !idProdutoOld.equals(idProdutoNew)) {
                idProdutoOld.getMovimentacaoCollection().remove(movimentacao);
                idProdutoOld = em.merge(idProdutoOld);
            }
            if (idProdutoNew != null && !idProdutoNew.equals(idProdutoOld)) {
                idProdutoNew.getMovimentacaoCollection().add(movimentacao);
                idProdutoNew = em.merge(idProdutoNew);
            }
            if (idUsuarioOld != null && !idUsuarioOld.equals(idUsuarioNew)) {
                idUsuarioOld.getMovimentacaoCollection().remove(movimentacao);
                idUsuarioOld = em.merge(idUsuarioOld);
            }
            if (idUsuarioNew != null && !idUsuarioNew.equals(idUsuarioOld)) {
                idUsuarioNew.getMovimentacaoCollection().add(movimentacao);
                idUsuarioNew = em.merge(idUsuarioNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = movimentacao.getIdMovimento();
                if (findMovimentacao(id) == null) {
                    throw new NonexistentEntityException("The movimentacao with id " + id + " no longer exists.");
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
            Movimentacao movimentacao;
            try {
                movimentacao = em.getReference(Movimentacao.class, id);
                movimentacao.getIdMovimento();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The movimentacao with id " + id + " no longer exists.", enfe);
            }
            Pessoa idPessoa = movimentacao.getIdPessoa();
            if (idPessoa != null) {
                idPessoa.getMovimentacaoCollection().remove(movimentacao);
                idPessoa = em.merge(idPessoa);
            }
            Produto idProduto = movimentacao.getIdProduto();
            if (idProduto != null) {
                idProduto.getMovimentacaoCollection().remove(movimentacao);
                idProduto = em.merge(idProduto);
            }
            Usuario idUsuario = movimentacao.getIdUsuario();
            if (idUsuario != null) {
                idUsuario.getMovimentacaoCollection().remove(movimentacao);
                idUsuario = em.merge(idUsuario);
            }
            em.remove(movimentacao);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Movimentacao> findMovimentacaoEntities() {
        return findMovimentacaoEntities(true, -1, -1);
    }

    public List<Movimentacao> findMovimentacaoEntities(int maxResults, int firstResult) {
        return findMovimentacaoEntities(false, maxResults, firstResult);
    }

    private List<Movimentacao> findMovimentacaoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Movimentacao.class));
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

    public Movimentacao findMovimentacao(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Movimentacao.class, id);
        } finally {
            em.close();
        }
    }

    public int getMovimentacaoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Movimentacao> rt = cq.from(Movimentacao.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
