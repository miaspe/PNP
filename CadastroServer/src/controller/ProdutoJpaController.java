
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
import javax.persistence.TypedQuery;
import model.Produto;

/**
 *
 * @author AlarconAbap
 */
public class ProdutoJpaController implements Serializable {

    public ProdutoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Produto produto) {
        if (produto.getMovimentacaoCollection() == null) {
            produto.setMovimentacaoCollection(new ArrayList<Movimentacao>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Movimentacao> attachedMovimentacaoCollection = new ArrayList<Movimentacao>();
            for (Movimentacao movimentacaoCollectionMovimentacaoToAttach : produto.getMovimentacaoCollection()) {
                movimentacaoCollectionMovimentacaoToAttach = em.getReference(movimentacaoCollectionMovimentacaoToAttach.getClass(), movimentacaoCollectionMovimentacaoToAttach.getIdMovimento());
                attachedMovimentacaoCollection.add(movimentacaoCollectionMovimentacaoToAttach);
            }
            produto.setMovimentacaoCollection(attachedMovimentacaoCollection);
            em.persist(produto);
            for (Movimentacao movimentacaoCollectionMovimentacao : produto.getMovimentacaoCollection()) {
                Produto oldIdProdutoOfMovimentacaoCollectionMovimentacao = movimentacaoCollectionMovimentacao.getIdProduto();
                movimentacaoCollectionMovimentacao.setIdProduto(produto);
                movimentacaoCollectionMovimentacao = em.merge(movimentacaoCollectionMovimentacao);
                if (oldIdProdutoOfMovimentacaoCollectionMovimentacao != null) {
                    oldIdProdutoOfMovimentacaoCollectionMovimentacao.getMovimentacaoCollection().remove(movimentacaoCollectionMovimentacao);
                    oldIdProdutoOfMovimentacaoCollectionMovimentacao = em.merge(oldIdProdutoOfMovimentacaoCollectionMovimentacao);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Produto produto) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Produto persistentProduto = em.find(Produto.class, produto.getIdProduto());
            Collection<Movimentacao> movimentacaoCollectionOld = persistentProduto.getMovimentacaoCollection();
            Collection<Movimentacao> movimentacaoCollectionNew = produto.getMovimentacaoCollection();
            Collection<Movimentacao> attachedMovimentacaoCollectionNew = new ArrayList<Movimentacao>();
            for (Movimentacao movimentacaoCollectionNewMovimentacaoToAttach : movimentacaoCollectionNew) {
                movimentacaoCollectionNewMovimentacaoToAttach = em.getReference(movimentacaoCollectionNewMovimentacaoToAttach.getClass(), movimentacaoCollectionNewMovimentacaoToAttach.getIdMovimento());
                attachedMovimentacaoCollectionNew.add(movimentacaoCollectionNewMovimentacaoToAttach);
            }
            movimentacaoCollectionNew = attachedMovimentacaoCollectionNew;
            produto.setMovimentacaoCollection(movimentacaoCollectionNew);
            produto = em.merge(produto);
            for (Movimentacao movimentacaoCollectionOldMovimentacao : movimentacaoCollectionOld) {
                if (!movimentacaoCollectionNew.contains(movimentacaoCollectionOldMovimentacao)) {
                    movimentacaoCollectionOldMovimentacao.setIdProduto(null);
                    movimentacaoCollectionOldMovimentacao = em.merge(movimentacaoCollectionOldMovimentacao);
                }
            }
            for (Movimentacao movimentacaoCollectionNewMovimentacao : movimentacaoCollectionNew) {
                if (!movimentacaoCollectionOld.contains(movimentacaoCollectionNewMovimentacao)) {
                    Produto oldIdProdutoOfMovimentacaoCollectionNewMovimentacao = movimentacaoCollectionNewMovimentacao.getIdProduto();
                    movimentacaoCollectionNewMovimentacao.setIdProduto(produto);
                    movimentacaoCollectionNewMovimentacao = em.merge(movimentacaoCollectionNewMovimentacao);
                    if (oldIdProdutoOfMovimentacaoCollectionNewMovimentacao != null && !oldIdProdutoOfMovimentacaoCollectionNewMovimentacao.equals(produto)) {
                        oldIdProdutoOfMovimentacaoCollectionNewMovimentacao.getMovimentacaoCollection().remove(movimentacaoCollectionNewMovimentacao);
                        oldIdProdutoOfMovimentacaoCollectionNewMovimentacao = em.merge(oldIdProdutoOfMovimentacaoCollectionNewMovimentacao);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = produto.getIdProduto();
                if (findProduto(id) == null) {
                    throw new NonexistentEntityException("O Produto de id " + id + " não existe.");
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
            Produto produto;
            try {
                produto = em.getReference(Produto.class, id);
                produto.getIdProduto();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("O Produto de id " + id + " não existe.", enfe);
            }
            Collection<Movimentacao> movimentacaoCollection = produto.getMovimentacaoCollection();
            for (Movimentacao movimentacaoCollectionMovimentacao : movimentacaoCollection) {
                movimentacaoCollectionMovimentacao.setIdProduto(null);
                movimentacaoCollectionMovimentacao = em.merge(movimentacaoCollectionMovimentacao);
            }
            em.remove(produto);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Produto> findProdutoEntities() {
        return findProdutoEntities(true, -1, -1);
    }

    public List<Produto> findProdutoEntities(int maxResults, int firstResult) {
        return findProdutoEntities(false, maxResults, firstResult);
    }

    private List<Produto> findProdutoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Produto.class));
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

    public Produto findProduto(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Produto.class, id);
        } finally {
            em.close();
        }
    }

    public int getProdutoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Produto> rt = cq.from(Produto.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

     public List<Produto> getListaProdutos() {
    EntityManager em = getEntityManager();
    try {
        CriteriaQuery<Produto> cq = em.getCriteriaBuilder().createQuery(Produto.class);
        cq.select(cq.from(Produto.class));
        TypedQuery<Produto> query = em.createQuery(cq);
        return query.getResultList();
    } finally {
        em.close();
    }
}
    
}
