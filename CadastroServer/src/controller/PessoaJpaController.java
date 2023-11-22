
package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import model.PessoaJuridica;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import model.PessoaFisica;
import model.Movimentacao;
import model.Pessoa;

/**
 *
 * @author Alarcon Abap
 */
public class PessoaJpaController implements Serializable {

    public PessoaJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Pessoa pessoa) {
        if (pessoa.getPessoaJuridicaCollection() == null) {
            pessoa.setPessoaJuridicaCollection(new ArrayList<PessoaJuridica>());
        }
        if (pessoa.getPessoaFisicaCollection() == null) {
            pessoa.setPessoaFisicaCollection(new ArrayList<PessoaFisica>());
        }
        if (pessoa.getMovimentacaoCollection() == null) {
            pessoa.setMovimentacaoCollection(new ArrayList<Movimentacao>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<PessoaJuridica> attachedPessoaJuridicaCollection = new ArrayList<PessoaJuridica>();
            for (PessoaJuridica pessoaJuridicaCollectionPessoaJuridicaToAttach : pessoa.getPessoaJuridicaCollection()) {
                pessoaJuridicaCollectionPessoaJuridicaToAttach = em.getReference(pessoaJuridicaCollectionPessoaJuridicaToAttach.getClass(), pessoaJuridicaCollectionPessoaJuridicaToAttach.getIdPessoaJuridica());
                attachedPessoaJuridicaCollection.add(pessoaJuridicaCollectionPessoaJuridicaToAttach);
            }
            pessoa.setPessoaJuridicaCollection(attachedPessoaJuridicaCollection);
            Collection<PessoaFisica> attachedPessoaFisicaCollection = new ArrayList<PessoaFisica>();
            for (PessoaFisica pessoaFisicaCollectionPessoaFisicaToAttach : pessoa.getPessoaFisicaCollection()) {
                pessoaFisicaCollectionPessoaFisicaToAttach = em.getReference(pessoaFisicaCollectionPessoaFisicaToAttach.getClass(), pessoaFisicaCollectionPessoaFisicaToAttach.getIdPessoaFisica());
                attachedPessoaFisicaCollection.add(pessoaFisicaCollectionPessoaFisicaToAttach);
            }
            pessoa.setPessoaFisicaCollection(attachedPessoaFisicaCollection);
            Collection<Movimentacao> attachedMovimentacaoCollection = new ArrayList<Movimentacao>();
            for (Movimentacao movimentacaoCollectionMovimentacaoToAttach : pessoa.getMovimentacaoCollection()) {
                movimentacaoCollectionMovimentacaoToAttach = em.getReference(movimentacaoCollectionMovimentacaoToAttach.getClass(), movimentacaoCollectionMovimentacaoToAttach.getIdMovimento());
                attachedMovimentacaoCollection.add(movimentacaoCollectionMovimentacaoToAttach);
            }
            pessoa.setMovimentacaoCollection(attachedMovimentacaoCollection);
            em.persist(pessoa);
            for (PessoaJuridica pessoaJuridicaCollectionPessoaJuridica : pessoa.getPessoaJuridicaCollection()) {
                Pessoa oldIdPessoaOfPessoaJuridicaCollectionPessoaJuridica = pessoaJuridicaCollectionPessoaJuridica.getIdPessoa();
                pessoaJuridicaCollectionPessoaJuridica.setIdPessoa(pessoa);
                pessoaJuridicaCollectionPessoaJuridica = em.merge(pessoaJuridicaCollectionPessoaJuridica);
                if (oldIdPessoaOfPessoaJuridicaCollectionPessoaJuridica != null) {
                    oldIdPessoaOfPessoaJuridicaCollectionPessoaJuridica.getPessoaJuridicaCollection().remove(pessoaJuridicaCollectionPessoaJuridica);
                    oldIdPessoaOfPessoaJuridicaCollectionPessoaJuridica = em.merge(oldIdPessoaOfPessoaJuridicaCollectionPessoaJuridica);
                }
            }
            for (PessoaFisica pessoaFisicaCollectionPessoaFisica : pessoa.getPessoaFisicaCollection()) {
                Pessoa oldIdPessoaOfPessoaFisicaCollectionPessoaFisica = pessoaFisicaCollectionPessoaFisica.getIdPessoa();
                pessoaFisicaCollectionPessoaFisica.setIdPessoa(pessoa);
                pessoaFisicaCollectionPessoaFisica = em.merge(pessoaFisicaCollectionPessoaFisica);
                if (oldIdPessoaOfPessoaFisicaCollectionPessoaFisica != null) {
                    oldIdPessoaOfPessoaFisicaCollectionPessoaFisica.getPessoaFisicaCollection().remove(pessoaFisicaCollectionPessoaFisica);
                    oldIdPessoaOfPessoaFisicaCollectionPessoaFisica = em.merge(oldIdPessoaOfPessoaFisicaCollectionPessoaFisica);
                }
            }
            for (Movimentacao movimentacaoCollectionMovimentacao : pessoa.getMovimentacaoCollection()) {
                Pessoa oldIdPessoaOfMovimentacaoCollectionMovimentacao = movimentacaoCollectionMovimentacao.getIdPessoa();
                movimentacaoCollectionMovimentacao.setIdPessoa(pessoa);
                movimentacaoCollectionMovimentacao = em.merge(movimentacaoCollectionMovimentacao);
                if (oldIdPessoaOfMovimentacaoCollectionMovimentacao != null) {
                    oldIdPessoaOfMovimentacaoCollectionMovimentacao.getMovimentacaoCollection().remove(movimentacaoCollectionMovimentacao);
                    oldIdPessoaOfMovimentacaoCollectionMovimentacao = em.merge(oldIdPessoaOfMovimentacaoCollectionMovimentacao);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Pessoa pessoa) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pessoa persistentPessoa = em.find(Pessoa.class, pessoa.getIdPessoa());
            Collection<PessoaJuridica> pessoaJuridicaCollectionOld = persistentPessoa.getPessoaJuridicaCollection();
            Collection<PessoaJuridica> pessoaJuridicaCollectionNew = pessoa.getPessoaJuridicaCollection();
            Collection<PessoaFisica> pessoaFisicaCollectionOld = persistentPessoa.getPessoaFisicaCollection();
            Collection<PessoaFisica> pessoaFisicaCollectionNew = pessoa.getPessoaFisicaCollection();
            Collection<Movimentacao> movimentacaoCollectionOld = persistentPessoa.getMovimentacaoCollection();
            Collection<Movimentacao> movimentacaoCollectionNew = pessoa.getMovimentacaoCollection();
            List<String> illegalOrphanMessages = null;
            for (PessoaJuridica pessoaJuridicaCollectionOldPessoaJuridica : pessoaJuridicaCollectionOld) {
                if (!pessoaJuridicaCollectionNew.contains(pessoaJuridicaCollectionOldPessoaJuridica)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("Você deve adicionar PessoaJuridica " + pessoaJuridicaCollectionOldPessoaJuridica + " seu id não pode ser apagável.");
                }
            }
            for (PessoaFisica pessoaFisicaCollectionOldPessoaFisica : pessoaFisicaCollectionOld) {
                if (!pessoaFisicaCollectionNew.contains(pessoaFisicaCollectionOldPessoaFisica)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("Você deve adicionar PessoaJuridica " + pessoaFisicaCollectionOldPessoaFisica + " seu id não pode ser apagável.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<PessoaJuridica> attachedPessoaJuridicaCollectionNew = new ArrayList<PessoaJuridica>();
            for (PessoaJuridica pessoaJuridicaCollectionNewPessoaJuridicaToAttach : pessoaJuridicaCollectionNew) {
                pessoaJuridicaCollectionNewPessoaJuridicaToAttach = em.getReference(pessoaJuridicaCollectionNewPessoaJuridicaToAttach.getClass(), pessoaJuridicaCollectionNewPessoaJuridicaToAttach.getIdPessoaJuridica());
                attachedPessoaJuridicaCollectionNew.add(pessoaJuridicaCollectionNewPessoaJuridicaToAttach);
            }
            pessoaJuridicaCollectionNew = attachedPessoaJuridicaCollectionNew;
            pessoa.setPessoaJuridicaCollection(pessoaJuridicaCollectionNew);
            Collection<PessoaFisica> attachedPessoaFisicaCollectionNew = new ArrayList<PessoaFisica>();
            for (PessoaFisica pessoaFisicaCollectionNewPessoaFisicaToAttach : pessoaFisicaCollectionNew) {
                pessoaFisicaCollectionNewPessoaFisicaToAttach = em.getReference(pessoaFisicaCollectionNewPessoaFisicaToAttach.getClass(), pessoaFisicaCollectionNewPessoaFisicaToAttach.getIdPessoaFisica());
                attachedPessoaFisicaCollectionNew.add(pessoaFisicaCollectionNewPessoaFisicaToAttach);
            }
            pessoaFisicaCollectionNew = attachedPessoaFisicaCollectionNew;
            pessoa.setPessoaFisicaCollection(pessoaFisicaCollectionNew);
            Collection<Movimentacao> attachedMovimentacaoCollectionNew = new ArrayList<Movimentacao>();
            for (Movimentacao movimentacaoCollectionNewMovimentacaoToAttach : movimentacaoCollectionNew) {
                movimentacaoCollectionNewMovimentacaoToAttach = em.getReference(movimentacaoCollectionNewMovimentacaoToAttach.getClass(), movimentacaoCollectionNewMovimentacaoToAttach.getIdMovimento());
                attachedMovimentacaoCollectionNew.add(movimentacaoCollectionNewMovimentacaoToAttach);
            }
            movimentacaoCollectionNew = attachedMovimentacaoCollectionNew;
            pessoa.setMovimentacaoCollection(movimentacaoCollectionNew);
            pessoa = em.merge(pessoa);
            for (PessoaJuridica pessoaJuridicaCollectionNewPessoaJuridica : pessoaJuridicaCollectionNew) {
                if (!pessoaJuridicaCollectionOld.contains(pessoaJuridicaCollectionNewPessoaJuridica)) {
                    Pessoa oldIdPessoaOfPessoaJuridicaCollectionNewPessoaJuridica = pessoaJuridicaCollectionNewPessoaJuridica.getIdPessoa();
                    pessoaJuridicaCollectionNewPessoaJuridica.setIdPessoa(pessoa);
                    pessoaJuridicaCollectionNewPessoaJuridica = em.merge(pessoaJuridicaCollectionNewPessoaJuridica);
                    if (oldIdPessoaOfPessoaJuridicaCollectionNewPessoaJuridica != null && !oldIdPessoaOfPessoaJuridicaCollectionNewPessoaJuridica.equals(pessoa)) {
                        oldIdPessoaOfPessoaJuridicaCollectionNewPessoaJuridica.getPessoaJuridicaCollection().remove(pessoaJuridicaCollectionNewPessoaJuridica);
                        oldIdPessoaOfPessoaJuridicaCollectionNewPessoaJuridica = em.merge(oldIdPessoaOfPessoaJuridicaCollectionNewPessoaJuridica);
                    }
                }
            }
            for (PessoaFisica pessoaFisicaCollectionNewPessoaFisica : pessoaFisicaCollectionNew) {
                if (!pessoaFisicaCollectionOld.contains(pessoaFisicaCollectionNewPessoaFisica)) {
                    Pessoa oldIdPessoaOfPessoaFisicaCollectionNewPessoaFisica = pessoaFisicaCollectionNewPessoaFisica.getIdPessoa();
                    pessoaFisicaCollectionNewPessoaFisica.setIdPessoa(pessoa);
                    pessoaFisicaCollectionNewPessoaFisica = em.merge(pessoaFisicaCollectionNewPessoaFisica);
                    if (oldIdPessoaOfPessoaFisicaCollectionNewPessoaFisica != null && !oldIdPessoaOfPessoaFisicaCollectionNewPessoaFisica.equals(pessoa)) {
                        oldIdPessoaOfPessoaFisicaCollectionNewPessoaFisica.getPessoaFisicaCollection().remove(pessoaFisicaCollectionNewPessoaFisica);
                        oldIdPessoaOfPessoaFisicaCollectionNewPessoaFisica = em.merge(oldIdPessoaOfPessoaFisicaCollectionNewPessoaFisica);
                    }
                }
            }
            for (Movimentacao movimentacaoCollectionOldMovimentacao : movimentacaoCollectionOld) {
                if (!movimentacaoCollectionNew.contains(movimentacaoCollectionOldMovimentacao)) {
                    movimentacaoCollectionOldMovimentacao.setIdPessoa(null);
                    movimentacaoCollectionOldMovimentacao = em.merge(movimentacaoCollectionOldMovimentacao);
                }
            }
            for (Movimentacao movimentacaoCollectionNewMovimentacao : movimentacaoCollectionNew) {
                if (!movimentacaoCollectionOld.contains(movimentacaoCollectionNewMovimentacao)) {
                    Pessoa oldIdPessoaOfMovimentacaoCollectionNewMovimentacao = movimentacaoCollectionNewMovimentacao.getIdPessoa();
                    movimentacaoCollectionNewMovimentacao.setIdPessoa(pessoa);
                    movimentacaoCollectionNewMovimentacao = em.merge(movimentacaoCollectionNewMovimentacao);
                    if (oldIdPessoaOfMovimentacaoCollectionNewMovimentacao != null && !oldIdPessoaOfMovimentacaoCollectionNewMovimentacao.equals(pessoa)) {
                        oldIdPessoaOfMovimentacaoCollectionNewMovimentacao.getMovimentacaoCollection().remove(movimentacaoCollectionNewMovimentacao);
                        oldIdPessoaOfMovimentacaoCollectionNewMovimentacao = em.merge(oldIdPessoaOfMovimentacaoCollectionNewMovimentacao);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = pessoa.getIdPessoa();
                if (findPessoa(id) == null) {
                    throw new NonexistentEntityException("A pessoa com id " + id + " não existe.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pessoa pessoa;
            try {
                pessoa = em.getReference(Pessoa.class, id);
                pessoa.getIdPessoa();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("A pessoa com id " + id + " não existe.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<PessoaJuridica> pessoaJuridicaCollectionOrphanCheck = pessoa.getPessoaJuridicaCollection();
            for (PessoaJuridica pessoaJuridicaCollectionOrphanCheckPessoaJuridica : pessoaJuridicaCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("A Pessoa (" + pessoa + ") Não pode ser apagado PessoaJuridica " + pessoaJuridicaCollectionOrphanCheckPessoaJuridica + " em seu campo pessoaJuridicaCollection possui um campo idPessoa não pode ser apagado.");
            }
            Collection<PessoaFisica> pessoaFisicaCollectionOrphanCheck = pessoa.getPessoaFisicaCollection();
            for (PessoaFisica pessoaFisicaCollectionOrphanCheckPessoaFisica : pessoaFisicaCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("A Pessoa (" + pessoa + ") Não pode ser apagado PessoaJuridica PessoaFisica " + pessoaFisicaCollectionOrphanCheckPessoaFisica + " em seu campo pessoaJuridicaCollection possui um campo idPessoa não pode ser apagado.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Movimentacao> movimentacaoCollection = pessoa.getMovimentacaoCollection();
            for (Movimentacao movimentacaoCollectionMovimentacao : movimentacaoCollection) {
                movimentacaoCollectionMovimentacao.setIdPessoa(null);
                movimentacaoCollectionMovimentacao = em.merge(movimentacaoCollectionMovimentacao);
            }
            em.remove(pessoa);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Pessoa> findPessoaEntities() {
        return findPessoaEntities(true, -1, -1);
    }

    public List<Pessoa> findPessoaEntities(int maxResults, int firstResult) {
        return findPessoaEntities(false, maxResults, firstResult);
    }

    private List<Pessoa> findPessoaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Pessoa.class));
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

    public Pessoa findPessoa(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Pessoa.class, id);
        } finally {
            em.close();
        }
    }

    public int getPessoaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Pessoa> rt = cq.from(Pessoa.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
