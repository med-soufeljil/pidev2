<?php

namespace App\Controller;

use App\Entity\Offre;
use App\Form\OffreType;
use App\Repository\OffreRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/offres')]
class OffreController extends AbstractController
{
    #[Route('', name: 'app_offre_index')]
    public function index(OffreRepository $repository): Response
    {
        return $this->render('offre/index.html.twig', ['items' => $repository->findAll()]);
    }

    #[Route('/new', name: 'app_offre_new')]
    public function create(Request $request, EntityManagerInterface $em): Response
    {
        return $this->handleForm($request, $em, new Offre());
    }

    #[Route('/{id}/edit', name: 'app_offre_edit')]
    public function edit(Offre $item, Request $request, EntityManagerInterface $em): Response
    {
        return $this->handleForm($request, $em, $item);
    }

    #[Route('/{id}/delete', name: 'app_offre_delete', methods: ['POST'])]
    public function delete(Offre $item, Request $request, EntityManagerInterface $em): Response
    {
        if ($this->isCsrfTokenValid('delete_offre_'.$item->getId(), (string) $request->request->get('_token'))) {
            $em->remove($item);
            $em->flush();
        }
        return $this->redirectToRoute('app_offre_index');
    }

    private function handleForm(Request $request, EntityManagerInterface $em, Offre $item): Response
    {
        $form = $this->createForm(OffreType::class, $item);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($item);
            $em->flush();
            return $this->redirectToRoute('app_offre_index');
        }

        return $this->render('offre/form.html.twig', ['form' => $form, 'item' => $item]);
    }
}
