<?php

namespace App\Controller;

use App\Entity\Candidat;
use App\Form\CandidatType;
use App\Repository\CandidatRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/candidats')]
class CandidatController extends AbstractController
{
    #[Route('', name: 'app_candidat_index')]
    public function index(CandidatRepository $repository): Response
    {
        return $this->render('candidat/index.html.twig', ['items' => $repository->findBy([], ['id' => 'DESC'])]);
    }

    #[Route('/new', name: 'app_candidat_new')]
    public function create(Request $request, EntityManagerInterface $em): Response
    {
        $item = new Candidat();
        return $this->handleForm($request, $em, $item);
    }

    #[Route('/{id}/edit', name: 'app_candidat_edit')]
    public function edit(Candidat $item, Request $request, EntityManagerInterface $em): Response
    {
        return $this->handleForm($request, $em, $item);
    }

    #[Route('/{id}/delete', name: 'app_candidat_delete', methods: ['POST'])]
    public function delete(Candidat $item, Request $request, EntityManagerInterface $em): Response
    {
        if ($this->isCsrfTokenValid('delete_candidat_'.$item->getId(), (string) $request->request->get('_token'))) {
            $em->remove($item);
            $em->flush();
        }
        return $this->redirectToRoute('app_candidat_index');
    }

    private function handleForm(Request $request, EntityManagerInterface $em, Candidat $item): Response
    {
        $form = $this->createForm(CandidatType::class, $item);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($item);
            $em->flush();
            return $this->redirectToRoute('app_candidat_index');
        }

        return $this->render('candidat/form.html.twig', ['form' => $form, 'item' => $item]);
    }
}
