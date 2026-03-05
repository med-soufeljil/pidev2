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
        $item = new Offre();
        $form = $this->createForm(OffreType::class, $item);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($item);
            $em->flush();
            return $this->redirectToRoute('app_offre_index');
        }

        return $this->render('offre/form.html.twig', ['form' => $form]);
    }
}
